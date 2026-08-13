package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.OvertimeDao;
import de.mamakow.dienstplanapotheke.model.Overtime;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class OvertimeRepository {
    private static final String TAG = "OvertimeRepository";
    private final OvertimeDao overtimeDao;
    private final RetrofitNetworkHandler networkHandler;
    private final SessionManager sessionManager;
    private final Executor executor;

    public OvertimeRepository(OvertimeDao overtimeDao, RetrofitNetworkHandler networkHandler, SessionManager sessionManager) {
        this.overtimeDao = overtimeDao;
        this.networkHandler = networkHandler;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void fetchAndSaveEmployeeOvertimes(int employeeKey, RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            if (callback != null) callback.onError("Nicht angemeldet.");
            return;
        }

        networkHandler.fetchEmployeeOvertimes(token, employeeKey, new RetrofitNetworkHandler.NetworkResponseCallback<List<Overtime>>() {
            @Override
            public void onSuccess(@NonNull List<Overtime> overtimes) {
                executor.execute(() -> {
                    overtimeDao.deleteOvertimesByEmployeeId(employeeKey);
                    overtimeDao.insertOvertimes(overtimes);
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Error fetching overtimes: " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
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
