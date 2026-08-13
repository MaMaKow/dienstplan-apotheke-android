package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.annotation.NonNull;
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

    public void fetchAndSaveAbsences(RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            sessionManager.performLogin();
            if (callback != null) callback.onError("Nicht angemeldet.");
            return;
        }

        networkHandler.fetchAbsences(token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Absence>>() {
            @Override
            public void onSuccess(@NonNull List<Absence> absences) {
                executor.execute(() -> {
                    absenceDao.clearAbsences();
                    absenceDao.insertAbsences(absences);
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Error fetching absences: " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    public void fetchAndSaveEmployeeAbsences(int employeeKey, int year, RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            if (callback != null) callback.onError("Nicht angemeldet.");
            return;
        }

        networkHandler.fetchEmployeeAbsences(token, employeeKey, year, new RetrofitNetworkHandler.NetworkResponseCallback<List<Absence>>() {
            @Override
            public void onSuccess(@NonNull List<Absence> absences) {
                executor.execute(() -> {
                    absenceDao.deleteAbsencesByEmployeeId(employeeKey);
                    absenceDao.insertAbsences(absences);
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Error fetching employee absences: " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }


    public LiveData<List<Absence>> getAllAbsencesByYearLiveData(int year) {
        return absenceDao.getAllAbsencesByYearLiveData(String.valueOf(year));
    }


    public LiveData<List<Absence>> getAbsencesByEmployeeIdAndYear(int employeeKey, int year) {
        return absenceDao.getAbsencesByEmployeeIdAndYear(employeeKey, String.valueOf(year));
    }
}
