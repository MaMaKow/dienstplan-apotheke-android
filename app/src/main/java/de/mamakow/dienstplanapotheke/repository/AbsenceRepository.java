package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.AbsenceDao;
import de.mamakow.dienstplanapotheke.model.Absence;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class AbsenceRepository {
    private static final String TAG = "AbsenceRepository";
    private final AbsenceDao absenceDao;
    private final RetrofitNetworkHandler networkHandler;
    private final SessionManager sessionManager;
    private final Executor executor;

    public AbsenceRepository(AbsenceDao absenceDao, RetrofitNetworkHandler networkHandler, SessionManager sessionManager) {
        this.absenceDao = absenceDao;
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

    public void fetchAndSaveAbsences() {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null, cannot fetch absences.");
            sessionManager.performLogin();
            return;
        }

        networkHandler.fetchAbsences(token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Absence>>() {
            @Override
            public void onSuccess(List<Absence> absences) {
                executor.execute(() -> {
                    absenceDao.clearAbsences();
                    absenceDao.insertAbsences(absences);
                    Log.d(TAG, "Absences saved to database: " + absences.size());
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleNetworkError(errorMessage);
            }
        });
    }

    public void fetchAndSaveEmployeeAbsences(int employeeKey) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            return;
        }

        networkHandler.fetchEmployeeAbsences(token, employeeKey, new RetrofitNetworkHandler.NetworkResponseCallback<List<Absence>>() {
            @Override
            public void onSuccess(List<Absence> absences) {
                executor.execute(() -> {
                    absenceDao.deleteAbsencesByEmployeeId(employeeKey);
                    absenceDao.insertAbsences(absences);
                    Log.d(TAG, "Employee absences saved to database: " + absences.size());
                });
            }

            @Override
            public void onError(String errorMessage) {
                handleNetworkError(errorMessage);
            }
        });
    }

    public LiveData<List<Absence>> getAllAbsencesLiveData() {
        return absenceDao.getAllAbsencesLiveData();
    }

    public LiveData<List<Absence>> getAbsencesByEmployeeId(int employeeKey) {
        return absenceDao.getAbsencesByEmployeeId(employeeKey);
    }

    public LiveData<List<Absence>> getAbsencesByEmployeeIdAndYear(int employeeKey, int year) {
        return absenceDao.getAbsencesByEmployeeIdAndYear(employeeKey, String.valueOf(year));
    }
}
