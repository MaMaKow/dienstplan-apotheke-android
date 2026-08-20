package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.AbsenceDao;
import de.mamakow.dienstplanapotheke.model.Absence;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class AbsenceRepository {
    private static final String TAG = "SYNC_DEBUG_ABSENCE_REP";
    private static final long SYNC_INTERVAL_SECONDS = 600; // 10 minutes
    private final AbsenceDao absenceDao;
    private final RetrofitNetworkHandler networkHandler;
    private final SessionManager sessionManager;
    private final Executor executor;
    // Cache for last sync timestamps: key = employeeKey + "_" + year
    private final Map<String, Long> lastSyncMap = new ConcurrentHashMap<>();

    public AbsenceRepository(AbsenceDao absenceDao, RetrofitNetworkHandler networkHandler, SessionManager sessionManager) {
        this.absenceDao = absenceDao;
        this.networkHandler = networkHandler;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void fetchAndSaveEmployeeAbsences(int employeeKey, int year, boolean force, RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
        String cacheKey = employeeKey + "_" + year;
        if (!force && !shouldSync(cacheKey)) {
            if (callback != null) callback.onSuccess(null);
            return;
        }

        String token = sessionManager.getSessionToken();
        if (token == null) {
            if (callback != null) callback.onError("Nicht angemeldet.");
            return;
        }

        Log.i(TAG, "Sync started for employee " + employeeKey + ", year " + year + " at " + Instant.now());
        networkHandler.fetchEmployeeAbsences(token, employeeKey, year, new RetrofitNetworkHandler.NetworkResponseCallback<List<Absence>>() {
            @Override
            public void onSuccess(@NonNull List<Absence> absences) {
                executor.execute(() -> {
                    absenceDao.syncAbsencesForEmployee(employeeKey, year, absences);
                    lastSyncMap.put(cacheKey, Instant.now().getEpochSecond());
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Sync error for employee " + employeeKey + ": " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    private boolean shouldSync(String cacheKey) {
        Long lastSync = lastSyncMap.get(cacheKey);
        if (lastSync == null) return true;
        return (Instant.now().getEpochSecond() - lastSync) > SYNC_INTERVAL_SECONDS;
    }

    public LiveData<List<Absence>> getAllAbsencesByYearLiveData(int year) {
        return absenceDao.getAllAbsencesByYearLiveData(String.valueOf(year));
    }

    public LiveData<List<Absence>> getAbsencesByEmployeeIdAndYear(int employeeKey, int year) {
        return absenceDao.getAbsencesByEmployeeIdAndYear(employeeKey, String.valueOf(year));
    }
}
