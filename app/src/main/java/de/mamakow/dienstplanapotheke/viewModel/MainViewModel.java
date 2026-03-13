package de.mamakow.dienstplanapotheke.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.repository.BranchRepository;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class MainViewModel extends AndroidViewModel {
    private final RosterRepository rosterRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        RetrofitNetworkHandler networkHandler = new RetrofitNetworkHandler(application);
        SessionManager sessionManager = new SessionManager(application);

        rosterRepository = new RosterRepository(networkHandler, db.rosterDao(), sessionManager);
        employeeRepository = new EmployeeRepository(db.employeeDao(), networkHandler, sessionManager);
        branchRepository = new BranchRepository(db.branchDao(), networkHandler, sessionManager);
    }

    public LiveData<Roster> getRoster() {
        return rosterRepository.getAllRosterData();
    }

    public LiveData<Roster> getRoster(LocalDate startDate, LocalDate endDate) {
        return rosterRepository.getRosterData(startDate, endDate);
    }

    public LiveData<List<Employee>> getEmployees() {
        return employeeRepository.getAllEmployeesLiveData();
    }

    public LiveData<List<Branch>> getBranches() {
        return branchRepository.getAllBranches();
    }

    public void refreshData(LocalDate startDate, LocalDate endDate, Integer employeeKey, Integer branchId) {
        employeeRepository.fetchAndSaveEmployees();
        branchRepository.fetchAndSaveBranches();
        rosterRepository.fetchAndSaveRosterData(startDate.toString(), endDate.toString(), employeeKey, branchId);
    }
}
