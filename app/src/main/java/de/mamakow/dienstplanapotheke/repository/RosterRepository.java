package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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

    public LiveData<Roster> getRosterData(LocalDate startDate, LocalDate endDate) {
        MutableLiveData<Roster> rosterLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            Roster roster = getRosterForDateRange(startDate, endDate);
            rosterLiveData.postValue(roster);
        });
        return rosterLiveData;
    }

    public Roster getRosterForDateRange(LocalDate startDate, LocalDate endDate) {
        Roster roster = new Roster();
        LiveData<List<RosterItem>> rosterItemsLiveData = rosterItemDao.getRosterItemsForDateRange(startDate, endDate);
        List<RosterItem> rosterItems = rosterItemsLiveData.getValue();

        if (rosterItems == null) {
            return roster;
        }

        // Group RosterItems by date to create RosterDays
        Map<LocalDate, RosterDay> rosterDayMap = new HashMap<>();
        for (RosterItem item : rosterItems) {
            LocalDate date = item.getLocalDate();
            RosterDay rosterDay = rosterDayMap.get(date);
            if (rosterDay == null) {
                rosterDay = new RosterDay(date);
                rosterDayMap.put(date, rosterDay);
            }
            rosterDay.addRosterItem(item);
        }

        // Add RosterDays to the Roster
        for (RosterDay rosterDay : rosterDayMap.values()) {
            roster.addRosterDay(rosterDay);
        }

        return roster;
    }

    public void fetchAndSaveRosterData() {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null");
            return;
        }
        retrofitNetworkHandler.fetchRoster(token, new RetrofitNetworkHandler.NetworkResponseCallback<Roster>() {
            @Override
            public void onSuccess(Roster roster) {
                executor.execute(() -> {
                    rosterItemDao.clearRosterItems();
                    List<RosterItem> rosterItems = new ArrayList<>();
                    for (RosterDay rosterDay : roster.getRosterDays()) {
                        rosterItems.addAll(rosterDay.getRosterItems());
                    }
                    rosterItemDao.insertRosterItems(rosterItems);
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching roster data: " + errorMessage);
            }
        });
    }
}