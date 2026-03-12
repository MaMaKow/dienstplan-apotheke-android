package de.mamakow.dienstplanapotheke.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.mamakow.dienstplanapotheke.database.EmployeeDao;
import de.mamakow.dienstplanapotheke.model.Employee;
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

    public void fetchAndSaveEmployees() {
        String token = sessionManager.getSessionToken();
        if (token == null) {
            Log.e(TAG, "Token is null, cannot fetch employees.");
            return;
        }

        networkHandler.fetchEmployees(token, new RetrofitNetworkHandler.NetworkResponseCallback<List<Employee>>() {
            @Override
            public void onSuccess(List<Employee> employees) {
                executor.execute(() -> {
                    employeeDao.clearEmployees();
                    employeeDao.insertEmployees(employees);
                    Log.d(TAG, "Employees saved to database: " + employees.size());
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching employees: " + errorMessage);
            }
        });
    }

    public List<Employee> getAllEmployees() {
        return employeeDao.getAllEmployees();
    }

    public LiveData<List<Employee>> getAllEmployeesLiveData() {
        return employeeDao.getAllEmployeesLiveData();
    }
}
