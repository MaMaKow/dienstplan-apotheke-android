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

import de.mamakow.dienstplanapotheke.database.OvertimeDao;
import de.mamakow.dienstplanapotheke.model.Overtime;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class OvertimeRepository {
    private static final String TAG = "SYNC_DEBUG_OVERTIME_REP";
    private static final long SYNC_INTERVAL_SECONDS = 600; // 10 minutes
    private final OvertimeDao overtimeDao;
    private final RetrofitNetworkHandler networkHandler;
    private final SessionManager sessionManager;
    private final Executor executor;
    // Cache for last sync timestamps: key = employeeKey + "_" + year
    private final Map<String, Long> lastSyncMap = new ConcurrentHashMap<>();

    public OvertimeRepository(OvertimeDao overtimeDao, RetrofitNetworkHandler networkHandler, SessionManager sessionManager) {
        this.overtimeDao = overtimeDao;
        this.networkHandler = networkHandler;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void fetchAndSaveEmployeeOvertimes(int employeeKey, int year, boolean force, RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
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

        networkHandler.fetchEmployeeOvertimes(token, employeeKey, new RetrofitNetworkHandler.NetworkResponseCallback<List<Overtime>>() {
            @Override
            public void onSuccess(@NonNull List<Overtime> overtimes) {
                executor.execute(() -> {
                    overtimeDao.syncOvertimesForEmployee(employeeKey, overtimes);
                    lastSyncMap.put(cacheKey, Instant.now().getEpochSecond());
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Error fetching overtimes for employee " + employeeKey + ": " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    private boolean shouldSync(String cacheKey) {
        Long lastSync = lastSyncMap.get(cacheKey);
        if (lastSync == null) return true;
        return (Instant.now().getEpochSecond() - lastSync) > SYNC_INTERVAL_SECONDS;
    }

    public LiveData<List<Overtime>> getAllOvertimesLiveData() {
        return overtimeDao.getAllOvertimesLiveData();
    }

    public LiveData<List<Overtime>> getOvertimesByEmployeeId(int employeeKey) {
        return overtimeDao.getOvertimesByEmployeeId(employeeKey);
    }

    public LiveData<List<Overtime>> getOvertimesByEmployeeIdAndYear(int employeeKey, int year) {
        return overtimeDao.getOvertimesByEmployeeIdAndYear(employeeKey, String.valueOf(year));
    }
}
