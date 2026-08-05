package de.mamakow.dienstplanapotheke.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.model.Absence;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Overtime;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.Workforce;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.repository.AbsenceRepository;
import de.mamakow.dienstplanapotheke.repository.BranchRepository;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;
import de.mamakow.dienstplanapotheke.repository.OvertimeRepository;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class MainViewModel extends AndroidViewModel {
    private final RosterRepository rosterRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final AbsenceRepository absenceRepository;
    private final OvertimeRepository overtimeRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Shared state for Fragments
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
    private final MutableLiveData<Branch> selectedBranch = new MutableLiveData<>();
    private final MutableLiveData<Employee> selectedEmployee = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        RetrofitNetworkHandler networkHandler = new RetrofitNetworkHandler(application);
        SessionManager sessionManager = new SessionManager(application);

        rosterRepository = new RosterRepository(networkHandler, db.rosterDao(), sessionManager);
        employeeRepository = new EmployeeRepository(db.employeeDao(), networkHandler, sessionManager);
        branchRepository = new BranchRepository(db.branchDao(), networkHandler, sessionManager);
        absenceRepository = new AbsenceRepository(db.absenceDao(), networkHandler, sessionManager);
        overtimeRepository = new OvertimeRepository(db.overtimeDao(), networkHandler, sessionManager);
        
        // Initialize selectedEmployee from SessionManager
        int employeeKey = sessionManager.getUserEmployeeKey();
        if (employeeKey != -1) {
            Employee e = new Employee();
            e.setEmployeeKey(employeeKey);
            e.setEmployeeFirstName(sessionManager.getUserDisplayName());
            selectedEmployee.setValue(e);
        }
    }

    public LiveData<Roster> getRoster() {
        return rosterRepository.getAllRosterData();
    }

    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public LiveData<Branch> getSelectedBranch() {
        return selectedBranch;
    }

    public void setSelectedBranch(Branch branch) {
        selectedBranch.setValue(branch);
    }

    public LiveData<Employee> getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee employee) {
        selectedEmployee.setValue(employee);
    }

    public LiveData<Workforce> getWorkforce() {
        return employeeRepository.getWorkforceLiveData();
    }

    public LiveData<List<Branch>> getBranches() {
        return branchRepository.getAllBranches();
    }

    public LiveData<List<Absence>> getAbsencesForEmployeeAndYear(int employeeKey, int year) {
        return absenceRepository.getAbsencesByEmployeeIdAndYear(employeeKey, year);
    }

    public LiveData<List<Overtime>> getOvertimesForEmployeeAndYear(int employeeKey, int year) {
        return overtimeRepository.getOvertimesByEmployeeIdAndYear(employeeKey, year);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchAllAbsences() {
        absenceRepository.fetchAndSaveAbsences();
    }

    public void fetchOvertimes(int employeeKey) {
        overtimeRepository.fetchAndSaveEmployeeOvertimes(employeeKey);
    }

    public void refreshData(LocalDate startDate, LocalDate endDate, Integer employeeKey, Integer branchId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        employeeRepository.fetchAndSaveEmployees();
        branchRepository.fetchAndSaveBranches();

        if (employeeKey == null && branchId == null) {
            isLoading.postValue(false);
        } else {
            rosterRepository.fetchAndSaveRosterData(startDate.toString(), endDate.toString(), employeeKey, branchId, new RetrofitNetworkHandler.NetworkResponseCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    isLoading.postValue(false);
                }

                @Override
                public void onError(String message) {
                    isLoading.postValue(false);
                    errorMessage.postValue(message);
                }
            });
        }

        if (employeeKey != null) {
            absenceRepository.fetchAndSaveEmployeeAbsences(employeeKey);
        }
    }
}
