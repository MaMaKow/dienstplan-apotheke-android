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

    /**
     * Gibt ein LiveData-Objekt zurück, das den Roster für einen Zeitraum darstellt.
     * Nutzt MediatorLiveData und einen Executor, um die Gruppierung im Hintergrund auszuführen,
     * damit der Main-Thread nicht blockiert wird.
     */
    public LiveData<Roster> getRosterData(LocalDate startDate, LocalDate endDate) {
        MediatorLiveData<Roster> result = new MediatorLiveData<>();
        LiveData<List<RosterItem>> source = rosterItemDao.getRosterItemsForDateRange(startDate, endDate);

        result.addSource(source, rosterItems -> {
            executor.execute(() -> {
                Roster roster = new Roster();
                if (rosterItems != null && !rosterItems.isEmpty()) {
                    // Gruppiere RosterItems nach Datum
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

                    // Füge die Tage dem Roster hinzu
                    for (RosterDay rosterDay : rosterDayMap.values()) {
                        roster.addRosterDay(rosterDay);
                    }
                }
                result.postValue(roster);
            });
        });

        return result;
    }

    public void fetchAndSaveRosterData(String dateStart, String dateEnd, int employeeKey) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null. Triggering login...");
            sessionManager.performLogin();
            return;
        }
        retrofitNetworkHandler.fetchRoster(token, dateStart, dateEnd, employeeKey, new RetrofitNetworkHandler.NetworkResponseCallback<List<RosterItem>>() {
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
                if (errorMessage != null && errorMessage.contains("Token expired")) {
                    Log.i(TAG, "Token expired detected. Clearing token and triggering re-login.");
                    sessionManager.logout();
                    sessionManager.performLogin();
                    // Optional: Nach dem Login erneut versuchen zu laden? 
                    // Das könnte komplex werden (Callback-Chain). 
                    // Fürs erste reicht der Re-Login.
                }
            }
        });
    }
}
