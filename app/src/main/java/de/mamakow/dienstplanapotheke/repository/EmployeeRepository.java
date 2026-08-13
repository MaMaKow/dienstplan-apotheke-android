package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.EmployeeDao;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Workforce;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class EmployeeRepository {
    private static final String TAG = "EmployeeRepository";
    private final EmployeeDao employeeDao;
    private final RetrofitNetworkHandler networkHandler;
    private final SessionManager sessionManager;
    private final Executor executor;

    public EmployeeRepository(EmployeeDao employeeDao, RetrofitNetworkHandler networkHandler, SessionManager sessionManager) {
        this.employeeDao = employeeDao;
        this.networkHandler = networkHandler;
        this.sessionManager = sessionManager;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Employee getEmployeeByEmployeeKey(int employeeKey) {
        return employeeDao.getEmployeeByEmployeeKey(employeeKey);
    }

    public Employee getEmployeeById(int id) {
        return employeeDao.getEmployeeById(id);
    }

    public void fetchAndSaveEmployees(RetrofitNetworkHandler.NetworkResponseCallback<Void> callback) {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            sessionManager.performLogin();
            if (callback != null) callback.onError("Nicht angemeldet.");
            return;
        }

        networkHandler.fetchEmployees(token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Employee>>() {
            @Override
            public void onSuccess(@NonNull List<Employee> employees) {
                executor.execute(() -> {
                    employeeDao.clearEmployees();
                    employeeDao.insertEmployees(employees);
                    if (callback != null) callback.onSuccess(null);
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Log.e(TAG, "Network error: " + errorMessage);
                if (callback != null) callback.onError(errorMessage);
            }
        });
    }

    public List<Employee> getAllEmployees() {
        return employeeDao.getAllEmployees();
    }

    public LiveData<Workforce> getWorkforceLiveData() {
        return Transformations.map(employeeDao.getAllEmployeesLiveData(), employees -> {
            return new Workforce(employees);
        });
    }
}
