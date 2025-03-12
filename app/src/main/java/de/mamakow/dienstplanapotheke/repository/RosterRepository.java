package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.RosterDao;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class RosterRepository {
    private static final String TAG = "RosterRepository";
    private final RetrofitNetworkHandler retrofitNetworkHandler;
    private final RosterDao rosterDao;
    private final SessionManager sessionManager;
    private final Executor executor;

    public RosterRepository(RetrofitNetworkHandler retrofitNetworkHandler, RosterDao rosterDao, SessionManager sessionManager) {
        this.retrofitNetworkHandler = retrofitNetworkHandler;
        this.rosterDao = rosterDao;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Roster>> getRosterData(LocalDate startDate, LocalDate endDate) {
        return rosterDao.getRoster(startDate, endDate);
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
                    rosterDao.clearRosters();
                    rosterDao.insertRoster(roster);
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching roster data: " + errorMessage);
            }
        });
    }

    /**
     * Parses the JSON string into a Roster object.
     *
     * @param jsonString
     * @return
     * @deprecated Wird nicht mehr benötigt, weil RetrofitNetworkHandler den alten NetworkHandler ersetzt.
     */
    private Roster parseRosterFromJson(String jsonString) {
        // Annahme: Das JSON ist ein Array von RosterDay-Objekten
        Type rosterDayListType = new TypeToken<List<RosterDay>>() {
        }.getType();
        Gson gson = new Gson();
        List<RosterDay> rosterDayList = gson.fromJson(jsonString, rosterDayListType);

        // Konvertiere die Liste von RosterDay-Objekten in ein Roster-Objekt
        Roster roster = new Roster();
        roster.setRosterDayList(rosterDayList);
        return roster;
    }
}

