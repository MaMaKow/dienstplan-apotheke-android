package de.mamakow.dienstplanapotheke.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

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
import de.mamakow.dienstplanapotheke.util.Event;
import de.mamakow.dienstplanapotheke.util.UIError;

public class MainViewModel extends AndroidViewModel {
    private final RosterRepository rosterRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final AbsenceRepository absenceRepository;
    private final OvertimeRepository overtimeRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<UIError>> uiError = new MutableLiveData<>();

    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
    private final MutableLiveData<Branch> selectedBranch = new MutableLiveData<>();
    private final MutableLiveData<Employee> selectedEmployee = new MutableLiveData<>();

    private final LiveData<Roster> employeeRoster;
    private final LiveData<Roster> branchRoster;
    private final LiveData<List<Absence>> absences;
    private final LiveData<List<Overtime>> overtimes;

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

        int employeeKey = sessionManager.getUserEmployeeKey();
        if (employeeKey != -1) {
            Employee e = new Employee();
            e.setEmployeeKey(employeeKey);
            e.setEmployeeFirstName(sessionManager.getUserDisplayName());
            selectedEmployee.setValue(e);
        }

        // Filters for reactive streams
        MediatorLiveData<Pair<LocalDate, Employee>> employeeFilter = new MediatorLiveData<>();
        employeeFilter.addSource(selectedDate, date -> employeeFilter.setValue(new Pair<>(date, selectedEmployee.getValue())));
        employeeFilter.addSource(selectedEmployee, emp -> employeeFilter.setValue(new Pair<>(selectedDate.getValue(), emp)));

        // 1. Reactive Roster (Mein Plan)
        employeeRoster = Transformations.switchMap(employeeFilter, filter -> {
            if (filter.second == null) return new MutableLiveData<>(new Roster());
            LocalDate start = filter.first != null ? filter.first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate end = start.plusDays(6);

            // Trigger background sync if needed (handled by Repository Policy)
            rosterRepository.fetchAndSaveRosterData(start.toString(), end.toString(), filter.second.getEmployeeKey(), null, false, null);

            return rosterRepository.getRosterData(start, end, filter.second.getEmployeeKey(), null);
        });

        // 2. Reactive Branch Roster
        MediatorLiveData<Pair<LocalDate, Branch>> branchFilter = new MediatorLiveData<>();
        branchFilter.addSource(selectedDate, date -> branchFilter.setValue(new Pair<>(date, selectedBranch.getValue())));
        branchFilter.addSource(selectedBranch, branch -> branchFilter.setValue(new Pair<>(selectedDate.getValue(), branch)));

        branchRoster = Transformations.switchMap(branchFilter, filter -> {
            if (filter.second == null) return new MutableLiveData<>(new Roster());
            LocalDate date = filter.first != null ? filter.first : LocalDate.now();

            // Trigger background sync if needed
            rosterRepository.fetchAndSaveRosterData(date.toString(), date.toString(), null, filter.second.getBranchId(), false, null);

            return rosterRepository.getRosterData(date, date, null, filter.second.getBranchId());
        });

        // 3. Reactive Absences
        absences = Transformations.switchMap(employeeFilter, filter -> {
            if (filter.second == null) return new MutableLiveData<>();
            int year = filter.first != null ? filter.first.getYear() : LocalDate.now().getYear();

            // Trigger background sync if needed
            absenceRepository.fetchAndSaveEmployeeAbsences(filter.second.getEmployeeKey(), year, false, null);

            return absenceRepository.getAbsencesByEmployeeIdAndYear(filter.second.getEmployeeKey(), year);
        });

        // 4. Reactive Overtimes
        overtimes = Transformations.switchMap(employeeFilter, filter -> {
            if (filter.second == null) return new MutableLiveData<>();
            int year = filter.first != null ? filter.first.getYear() : LocalDate.now().getYear();

            // Trigger background sync if needed
            overtimeRepository.fetchAndSaveEmployeeOvertimes(filter.second.getEmployeeKey(), year, false, null);

            return overtimeRepository.getOvertimesByEmployeeIdAndYear(filter.second.getEmployeeKey(), year);
        });
    }

    public LiveData<Roster> getEmployeeRoster() {
        return employeeRoster;
    }

    public LiveData<Roster> getBranchRoster() {
        return branchRoster;
    }

    public LiveData<List<Absence>> getAbsences() {
        return absences;
    }

    public LiveData<List<Overtime>> getOvertimes() {
        return overtimes;
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

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Event<UIError>> getUiError() {
        return uiError;
    }

    public void fetchOvertimes(int employeeKey) {
        int year = selectedDate.getValue() != null ? selectedDate.getValue().getYear() : LocalDate.now().getYear();
        overtimeRepository.fetchAndSaveEmployeeOvertimes(employeeKey, year, true, new RetrofitNetworkHandler.NetworkResponseCallback<Void>() {
            @Override
            public void onSuccess(@NonNull Void data) {
            }

            @Override
            public void onError(@NonNull String message) {
                postError(message, UIError.Type.TOAST, () -> fetchOvertimes(employeeKey));
            }
        });
    }

    public void refreshData(boolean force) {
        isLoading.setValue(true);

        LocalDate now = LocalDate.now();
        LocalDate syncStart = now.minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate syncEnd = now.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        Employee emp = selectedEmployee.getValue();
        Branch branch = selectedBranch.getValue();
        LocalDate date = selectedDate.getValue();
        int year = (date != null) ? date.getYear() : now.getYear();

        employeeRepository.fetchAndSaveEmployees(null);
        branchRepository.fetchAndSaveBranches(null);

        if (emp != null) {
            rosterRepository.fetchAndSaveRosterData(syncStart.toString(), syncEnd.toString(), emp.getEmployeeKey(), null, force, new RetrofitNetworkHandler.NetworkResponseCallback<Void>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    isLoading.postValue(false);
                }

                @Override
                public void onError(@NonNull String message) {
                    isLoading.postValue(false);
                    postError(message, UIError.Type.SNACKBAR_WITH_RETRY, () -> refreshData(true));
                }
            });
            absenceRepository.fetchAndSaveEmployeeAbsences(emp.getEmployeeKey(), year, force, null);
            overtimeRepository.fetchAndSaveEmployeeOvertimes(emp.getEmployeeKey(), year, force, null);
        }

        if (branch != null) {
            rosterRepository.fetchAndSaveRosterData(syncStart.toString(), syncEnd.toString(), null, branch.getBranchId(), force, new RetrofitNetworkHandler.NetworkResponseCallback<Void>() {
                @Override
                public void onSuccess(@NonNull Void data) {
                    isLoading.postValue(false);
                }

                @Override
                public void onError(@NonNull String message) {
                    isLoading.postValue(false);
                }
            });
        }

        if (emp == null && branch == null) {
            isLoading.setValue(false);
        }
    }

    private void postError(String message, UIError.Type type, Runnable retryAction) {
        uiError.postValue(new Event<>(new UIError(message, type, retryAction)));
    }

    private static class Pair<A, B> {
        public final A first;
        public final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}
