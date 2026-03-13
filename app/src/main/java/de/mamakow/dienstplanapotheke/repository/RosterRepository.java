package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.RosterItemDao;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class RosterRepository {
    private static final String TAG = "RosterRepository";
    private final RetrofitNetworkHandler retrofitNetworkHandler;
    private final RosterItemDao rosterItemDao;
    private final SessionManager sessionManager;
    private final Executor executor;

    public RosterRepository(RetrofitNetworkHandler retrofitNetworkHandler, RosterItemDao rosterItemDao, SessionManager sessionManager) {
        this.retrofitNetworkHandler = retrofitNetworkHandler;
        this.rosterItemDao = rosterItemDao;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<Roster> getAllRosterData() {
        MediatorLiveData<Roster> result = new MediatorLiveData<>();
        LiveData<List<RosterItem>> source = rosterItemDao.getAllRosterItems();

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

    public LiveData<Roster> getRosterData(LocalDate startDate, LocalDate endDate) {
        MediatorLiveData<Roster> result = new MediatorLiveData<>();
        LiveData<List<RosterItem>> source = rosterItemDao.getRosterItemsForDateRange(startDate, endDate);

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

    public void fetchAndSaveRosterData(String dateStart, String dateEnd, Integer employeeKey, Integer branchId) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null. Triggering login...");
            sessionManager.performLogin();
            return;
        }
        retrofitNetworkHandler.fetchRoster(token, dateStart, dateEnd, employeeKey, branchId, new RetrofitNetworkHandler.NetworkResponseCallback<List<RosterItem>>() {
            @Override
            public void onSuccess(List<RosterItem> rosterItems) {
                executor.execute(() -> {
                    rosterItemDao.clearRosterItems();
                    if (rosterItems != null && !rosterItems.isEmpty()) {
                        rosterItemDao.insertRosterItems(rosterItems);
                        Log.d(TAG, "Roster data saved to database: " + rosterItems.size() + " items.");
                    } else {
                        Log.d(TAG, "No roster items received from server.");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching roster data: " + errorMessage);
                if (errorMessage != null && (errorMessage.contains("Token expired") || errorMessage.contains("Invalid token"))) {
                    Log.i(TAG, "Token issue detected. Triggering re-login.");
                    sessionManager.logout();
                    sessionManager.performLogin();
                }
            }
        });
    }
}
