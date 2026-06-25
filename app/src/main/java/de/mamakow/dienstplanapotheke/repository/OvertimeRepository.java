package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

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

    private void handleNetworkError(String errorMessage) {
        Log.e(TAG, "Network error: " + errorMessage);
        if (errorMessage != null && (errorMessage.contains("Invalid token") || errorMessage.contains("Token expired"))) {
            Log.i(TAG, "Token issue detected. Triggering re-login.");
            sessionManager.logout();
            sessionManager.performLogin();
        }
    }

    public void fetchAndSaveOvertimes(int employeeKey) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null, cannot fetch overtimes.");
            sessionManager.performLogin();
            return;
        }

        networkHandler.fetchEmployeeOvertimes(employeeKey, token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Overtime>>() {
            @Override
            public void onSuccess(List<Overtime> overtimes) {
                executor.execute(() -> {
                    overtimeDao.clearOvertimes();
                    overtimeDao.insertOvertimes(overtimes);
                    Log.d(TAG, "Overtimes saved to database: " + overtimes.size());
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleNetworkError(errorMessage);
            }
        });
    }

    public void fetchAndSaveEmployeeOvertimes(int employeeKey) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            return;
        }

        networkHandler.fetchEmployeeOvertimes(token, employeeKey, new RetrofitNetworkHandler.NetworkResponseCallback<List<Overtime>>() {
            @Override
            public void onSuccess(List<Overtime> overtimes) {
                executor.execute(() -> {
                    overtimeDao.deleteOvertimesByEmployeeId(employeeKey);
                    overtimeDao.insertOvertimes(overtimes);
                    Log.d(TAG, "Employee overtimes saved to database: " + overtimes.size());
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleNetworkError(errorMessage);
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
