package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.RosterItemDao;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class RosterRepository {
    private static final String TAG = "SYNC_DEBUG_ROSTER_REP";
    private static final long SYNC_INTERVAL_SECONDS = 600; // 10 minutes
    private final RetrofitNetworkHandler retrofitNetworkHandler;
    private final RosterItemDao rosterItemDao;
    private final SessionManager sessionManager;
    private final Executor executor;
    // Cache for last sync timestamps: key = (employee_ID or branch_ID) + "_" + dateStart + "_" + dateEnd
    private final Map<String, Long> lastSyncMap = new ConcurrentHashMap<>();

    public RosterRepository(RetrofitNetworkHandler retrofitNetworkHandler, RosterItemDao rosterItemDao, SessionManager sessionManager) {
        this.retrofitNetworkHandler = retrofitNetworkHandler;
        this.rosterItemDao = rosterItemDao;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<Roster> getRosterData(LocalDate startDate, LocalDate endDate, Integer employeeKey, Integer branchId) {
        MediatorLiveData<Roster> result = new MediatorLiveData<>();
        LiveData<List<RosterItem>> source;

        if (employeeKey != null) {
            source = rosterItemDao.getRosterItemsForEmployeeAndDateRange(employeeKey, startDate, endDate);
        } else if (branchId != null) {
            source = rosterItemDao.getRosterItemsForBranchAndDateRange(branchId, startDate, endDate);
        } else {
            source = rosterItemDao.getRosterItemsForDateRange(startDate, endDate);
        }

        result.addSource(source, rosterItems -> {
            executor.execute(() -> {
                Roster roster = new Roster();
                if (rosterItems != null && !rosterItems.isEmpty()) {
                    Map<LocalDate, RosterDay> rosterDayMap = new LinkedHashMap<>();
                    for (RosterItem item : rosterItems) {
                        LocalDate date = item.getLocalDate();
                        RosterDay rosterDay = rosterDayMap.get(date);
                        if (rosterDay == null) {
                            rosterDay = new RosterDay(date);
                            rosterDayMap.put(date, rosterDay);
                        }
                        rosterDay.addRosterItem(item);
                    }
                    for (RosterDay rosterDay : rosterDayMap.values()) {
                        roster.addRosterDay(rosterDay);
                    }
                }
                result.postValue(roster);
            });
        });

        return result;
    }

    public void fetchAndSaveRosterData(String dateStart, String dateEnd, Integer employeeKey, Integer branchId, boolean force, RetrofitNetworkHandler.NetworkResponseCallback<Void> finalCallback) {
        String cacheKey = (employeeKey != null ? "emp_" + employeeKey : "br_" + branchId) + "_" + dateStart + "_" + dateEnd;
        if (!force && !shouldSync(cacheKey)) {
            if (finalCallback != null) finalCallback.onSuccess(null);
            return;
        }

        String token = sessionManager.getSessionToken();
        if (token == null) {
            sessionManager.performLogin();
            if (finalCallback != null) finalCallback.onError("Token is null");
            return;
        }

        Log.i(TAG, "Sync started for " + (employeeKey != null ? "Employee " + employeeKey : "Branch " + branchId)
                + " range: " + dateStart + " to " + dateEnd + " at " + Instant.now());

        retrofitNetworkHandler.fetchRoster(token, dateStart, dateEnd, employeeKey, branchId, new RetrofitNetworkHandler.NetworkResponseCallback<List<RosterItem>>() {
            @Override
            public void onSuccess(@NonNull List<RosterItem> rosterItems) {
                executor.execute(() -> {
                    LocalDate start = LocalDate.parse(dateStart);
                    LocalDate end = LocalDate.parse(dateEnd);

                    if (employeeKey != null) {
                        rosterItemDao.syncRosterItemsForEmployee(employeeKey, start, end, rosterItems);
                    } else if (branchId != null) {
                        rosterItemDao.syncRosterItemsForBranch(branchId, start, end, rosterItems);
                    } else {
                        rosterItemDao.insertRosterItems(rosterItems);
                    }

                    lastSyncMap.put(cacheKey, Instant.now().getEpochSecond());
                    if (finalCallback != null) finalCallback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Sync error: " + errorMessage);
                if (finalCallback != null) finalCallback.onError(errorMessage);
            }
        });
    }

    private boolean shouldSync(String cacheKey) {
        Long lastSync = lastSyncMap.get(cacheKey);
        if (lastSync == null) return true;
        return (Instant.now().getEpochSecond() - lastSync) > SYNC_INTERVAL_SECONDS;
    }
}
