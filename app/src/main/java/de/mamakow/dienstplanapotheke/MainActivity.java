package de.mamakow.dienstplanapotheke;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Workforce;
import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.view.AbsenceAdapter;
import de.mamakow.dienstplanapotheke.view.BranchRosterAdapter;
import de.mamakow.dienstplanapotheke.view.HeatmapFragment;
import de.mamakow.dienstplanapotheke.view.RosterAdapter;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
    private MainViewModel viewModel;
    private RecyclerView recyclerView;
    private View fragmentContainer;
    private RosterAdapter rosterAdapter;
    private BranchRosterAdapter branchRosterAdapter;

    private AbsenceAdapter absenceAdapter;

    private MaterialButtonToggleGroup viewModeToggleGroup;
    private Button buttonDatePicker;
    private ImageButton buttonPrevDate;
    private ImageButton buttonNextDate;
    private Spinner branchSpinner;
    private Spinner employeeSpinner;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;


    private LocalDate selectedDate;
    private Branch selectedBranch;
    private Employee selectedEmployee;

    private List<Branch> allBranches = new ArrayList<>();

    private Workforce currentWorkforce;
    private ViewMode currentViewMode = ViewMode.EMPLOYEE;
    private boolean isInitialDropdownSetup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initiales Datum setzen
        selectedDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        initViews();


        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isBaseUrlSet()) {
            showUrlInputDialog(sessionManager);
        } else {
            checkLoginAndProceed(sessionManager);
        }
    }

    private void checkLoginAndProceed(SessionManager sessionManager) {
        if (sessionManager.isNotLoggedIn()) {
            showLoginDialog(sessionManager);
        } else {
            Log.d(TAG, "checkLoginAndProceed: Login token vorhanden, prüfe auf User Data.");
            if (!sessionManager.hasUserData()) {
                Log.d(TAG, "checkLoginAndProceed: Login token vorhanden, User Data fehlt aber.");
                // Token ist da, aber Details fehlen (z.B. nach App-Neustart)
                sessionManager.refreshSessionUserData(new LoginCallback() {
                    @Override
                    public void onSuccess(String token) {
                        // Jetzt erst die restlichen Daten laden,
                        // damit der richtige Mitarbeiter vorselektiert wird
                        proceedWithInitialization(sessionManager);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        // Fehlerbehandlung
                        Log.e(TAG, "Fehler beim Laden der Benutzerdaten: " + exception.getLocalizedMessage());
                    }
                });
            } else {
                Log.d(TAG, "checkLoginAndProceed: Login token vorhanden, User Data war auch da.");
                proceedWithInitialization(sessionManager);
            }
        }
    }

    private void showLoginDialog(SessionManager sessionManager) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null);
        EditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);

        new AlertDialog.Builder(this)
                .setTitle(R.string.login_title)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(R.string.login_button, (dialog, which) -> {
                    String username = editTextUsername.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    if (!username.isEmpty() && !password.isEmpty()) {
                        sessionManager.performLogin(username, password, new LoginCallback() {
                            @Override
                            public void onSuccess(String token) {
                                runOnUiThread(() -> proceedWithInitialization(sessionManager));
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                                    showLoginDialog(sessionManager);
                                });
                            }
                        });
                    } else {
                        showLoginDialog(sessionManager);
                    }
                })
                .show();
    }

    private void proceedWithInitialization(SessionManager sessionManager) {
        setupRecyclerView();
        setupViewModel(sessionManager);
        setupListeners();

        updateUI();
        refreshData();
    }

    private void showUrlInputDialog(SessionManager sessionManager) {
        final EditText input = new EditText(this);
        input.setHint("https://ihre-domain.de/dienstplan/");
        input.setText(getString(R.string.test_page_url));

        new AlertDialog.Builder(this)
                .setTitle("API URL konfigurieren")
                .setMessage("Bitte geben Sie die Basis-URL Ihres Dienstplans ein:")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Speichern", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        sessionManager.saveBaseUrl(url);
                        checkLoginAndProceed(sessionManager);
                    } else {
                        showUrlInputDialog(sessionManager); // Erneut fragen, wenn leer
                    }
                })
                .show();
    }

    private void initViews() {

        viewModeToggleGroup = findViewById(R.id.viewModeToggleGroup);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        buttonPrevDate = findViewById(R.id.buttonPrevDate);
        buttonNextDate = findViewById(R.id.buttonNextDate);
        branchSpinner = findViewById(R.id.branchSpinner);
        employeeSpinner = findViewById(R.id.employeeSpinner);
        recyclerView = findViewById(R.id.recyclerViewRoster);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rosterAdapter = new RosterAdapter();
        branchRosterAdapter = new BranchRosterAdapter();
        absenceAdapter = new AbsenceAdapter();
        recyclerView.setAdapter(rosterAdapter);
    }

    private void setupViewModel(SessionManager sessionManager) {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getWorkforce().observe(this, workforce -> {
            if (workforce != null) {
                this.currentWorkforce = workforce;
                branchRosterAdapter.setEmployees(workforce);
                updateEmployeeSpinner(sessionManager);
                if (currentViewMode == ViewMode.ABSENCE) {
                    observeAbsences();
                }
            }
        });

        viewModel.getBranches().observe(this, branches -> {
            if (branches != null && !branches.equals(allBranches)) {
                this.allBranches = branches;
                rosterAdapter.setBranches(branches);
                updateBranchSpinner();
            }
        });

        viewModel.getRoster().observe(this, roster -> {
            if (roster != null && currentViewMode == ViewMode.EMPLOYEE) {
                Log.d(TAG, "Dienstplan-Update: " + roster.getRosterDays().size() + " Tage angezeigt.");
                rosterAdapter.setRosterDays(roster.getRosterDays());
            }
            if (roster != null && currentViewMode == ViewMode.BRANCH) {
                Log.d(TAG, "Dienstplan-Update für branchRosterAdapter.");
                branchRosterAdapter.setRosterDays(roster.getRosterDays());
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void observeAbsences() {
        if (selectedEmployee != null) {
            viewModel.getAbsencesForEmployeeAndYear(selectedEmployee.getEmployeeKey(), selectedDate.getYear())
                    .observe(this, absences -> {
                        if (currentViewMode == ViewMode.ABSENCE && absences != null) {
                            absenceAdapter.setAbsences(absences);
                        }
                    });
        }
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        viewModeToggleGroup.addOnButtonCheckedListener((viewModeToggleGroup, checkedId, isChecked) -> {
            if (!isChecked) {
                /*
                 * Damit der Listener nicht sowohl checked als auch unchecked state hier weitergibt.
                 * Wir interessieren uns nur für den jetzt neu checked state.
                 */
                return;
            }
            if (checkedId == R.id.btnBranchView) {
                currentViewMode = ViewMode.BRANCH;
            } else if (checkedId == R.id.btnAbsenceView) {
                currentViewMode = ViewMode.ABSENCE;
            } else if (checkedId == R.id.btnAbsenceHeatmapView) {
                currentViewMode = ViewMode.TEAM_HEATMAP;
            } else if (checkedId == R.id.btnLogout) {
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.logout();
                Intent intent = new Intent(this, MainActivity.class);
                // 2. Clear the activity stack so the user can't "Back" into the logged-in state
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return; // Important: prevent the rest of the listener from running
            } else { //R.id.btnEmployeeView
                currentViewMode = ViewMode.EMPLOYEE;
            }
            updateViewModeUI();
            updateUI();
            refreshData();
        });


        buttonDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                updateUI();
                refreshData();
            }, selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            datePicker.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
            datePicker.show();
        });

        buttonPrevDate.setOnClickListener(v -> {
            if (currentViewMode == ViewMode.BRANCH) {
                selectedDate = selectedDate.minusDays(1);
            } else if (currentViewMode == ViewMode.ABSENCE || currentViewMode == ViewMode.TEAM_HEATMAP) {
                selectedDate = selectedDate.minusYears(1);
            } else {
                selectedDate = selectedDate.minusWeeks(1);
            }
            updateUI();
            refreshData();
        });

        buttonNextDate.setOnClickListener(v -> {
            if (currentViewMode == ViewMode.BRANCH) {
                selectedDate = selectedDate.plusDays(1);
            } else if (currentViewMode == ViewMode.ABSENCE || currentViewMode == ViewMode.TEAM_HEATMAP) {
                selectedDate = selectedDate.plusYears(1);
            } else {
                selectedDate = selectedDate.plusWeeks(1);
            }
            updateUI();
            refreshData();
        });

        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Branch newSelection = allBranches.get(position);
                if (selectedBranch == null || selectedBranch.getBranchId() != newSelection.getBranchId()) {
                    selectedBranch = newSelection;
                    updateUI();
                    refreshData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected(position=" + position + ", initial=" + isInitialDropdownSetup + ")");
                /*
                 * CAVE: The index of  currentWorkforce.getEmployees().get() is safely equal
                 * to the spinner index (position), only as long as the
                 * spinner is always updated at the same time as the workforce.
                 */
                if (isInitialDropdownSetup) {
                    isInitialDropdownSetup = false;
                    // Optional: Hier prüfen, ob die Position bereits dem eingeloggten User entspricht
                    return;
                }
                Employee newSelection = currentWorkforce.getEmployees().get(position);
                Log.d(TAG, "selectedEmployee ALT = " + (selectedEmployee == null ? "null" : selectedEmployee.getEmployeeFullName()));

                if (selectedEmployee == null || selectedEmployee.getEmployeeKey() != newSelection.getEmployeeKey()) {
                    selectedEmployee = newSelection;
                    Log.d(TAG, "selectedEmployee NEU = " + newSelection.getEmployeeFullName());
                    updateUI();
                    if (currentViewMode == ViewMode.ABSENCE) {
                        observeAbsences();
                    }
                    refreshData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateViewModeUI() {
        recyclerView.setVisibility(currentViewMode == ViewMode.TEAM_HEATMAP ? View.GONE : View.VISIBLE);
        fragmentContainer.setVisibility(currentViewMode == ViewMode.TEAM_HEATMAP ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setEnabled(currentViewMode != ViewMode.TEAM_HEATMAP);

        if (currentViewMode == ViewMode.TEAM_HEATMAP) {
            showFragment(new HeatmapFragment());
        } else if (currentViewMode == ViewMode.ABSENCE) {
            recyclerView.setAdapter(absenceAdapter);
            observeAbsences();
        } else if (currentViewMode == ViewMode.BRANCH) {
            recyclerView.setAdapter(branchRosterAdapter);
        } else {
            recyclerView.setAdapter(rosterAdapter);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void updateBranchSpinner() {
        if (allBranches.isEmpty()) return;

        List<String> branchNames = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < allBranches.size(); i++) {
            Branch b = allBranches.get(i);
            branchNames.add(b.getBranchName());
            if (selectedBranch != null && b.getBranchId() == selectedBranch.getBranchId()) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branchNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        branchSpinner.setAdapter(spinnerAdapter);
        branchSpinner.setSelection(selectedIndex);

        if (selectedBranch == null) {
            selectedBranch = allBranches.get(0);
        }
    }

    private void updateEmployeeSpinner(SessionManager sessionManager) {
        Log.d(TAG, "updateEmployeeSpinner() BEGIN");
        List<Employee> employees = currentWorkforce.getEmployees();
        if (employees.isEmpty()) {
            Log.d(TAG, "updateEmployeeSpinner: employees is empty");
            return;
        }
        if (selectedEmployee == null) {
            Log.d(TAG, "selectedEmployee is null, setting it to logged in employee ");
            int loggedInKey = sessionManager.getUserEmployeeKey();
            Log.d(TAG, "loggedInKey: " + loggedInKey);
            if (loggedInKey != -1) {
                selectedEmployee = currentWorkforce.findByKey(loggedInKey);
                Log.d(TAG, "Trigger refreshData after initial employee selection");
                refreshData();
            } else {
                Log.d(TAG, "selectedEmployee is null, setting it to first employee in list");
                selectedEmployee = employees.get(0);
                refreshData();
            }
            Log.d(TAG, "selectedEmployee = " + selectedEmployee.getEmployeeFullName());
        }
        int selectedIndex = 0;
        List<String> employeeNames = currentWorkforce.getEmployeeNames();
        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            if (selectedEmployee != null && employee.getEmployeeKey() == selectedEmployee.getEmployeeKey()) {
                Log.d(TAG, "selectedEmployee: " + employee.getEmployeeKey() + " matches employee.getEmployeeKey(): " + selectedEmployee.getEmployeeKey());
                Log.d(TAG, "Set selectedIndex to " + i);
                selectedIndex = i;
            } else {
                Log.d(TAG, "selectedEmployee: " + selectedEmployee + " does not match employee.getEmployeeKey(): " + employee.getEmployeeKey());
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, employeeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Log.d(TAG, "selectedEmployee vorher = " + selectedEmployee);
        employeeSpinner.setAdapter(spinnerAdapter);
        Log.d(TAG, "Adapter gesetzt");
        employeeSpinner.setSelection(selectedIndex, false);
        Log.d(TAG, "Selection gesetzt: " + selectedIndex);
    }

    private void updateUI() {
        if (currentViewMode == ViewMode.BRANCH) {
            buttonDatePicker.setText(selectedDate.format(dateFormatter));
            branchSpinner.setVisibility(View.VISIBLE);
            employeeSpinner.setVisibility(View.GONE);
        } else if (currentViewMode == ViewMode.ABSENCE) {
            buttonDatePicker.setText(String.valueOf(selectedDate.getYear()));
            branchSpinner.setVisibility(View.GONE);
            employeeSpinner.setVisibility(View.VISIBLE);
        } else if (currentViewMode == ViewMode.TEAM_HEATMAP) {
            buttonDatePicker.setText(String.valueOf(selectedDate.getYear()));
            branchSpinner.setVisibility(View.GONE);
            employeeSpinner.setVisibility(View.GONE);
        } else {
            LocalDate monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            buttonDatePicker.setText(String.format("%s %s", getString(R.string.woche_vom), monday.format(dateFormatter)));
            branchSpinner.setVisibility(View.GONE);
            employeeSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void refreshData() {
        if (viewModel == null) return; // Noch nicht initialisiert

        LocalDate startDate, endDate;
        Integer employeeKey = null;
        Integer branchId = null;

        if (currentViewMode == ViewMode.BRANCH) {
            startDate = selectedDate;
            endDate = selectedDate;
            branchId = (selectedBranch != null) ? selectedBranch.getBranchId() : null;
        } else if (currentViewMode == ViewMode.ABSENCE) {
            startDate = LocalDate.of(selectedDate.getYear(), 1, 1);
            endDate = LocalDate.of(selectedDate.getYear(), 12, 31);
            employeeKey = (selectedEmployee != null) ? selectedEmployee.getEmployeeKey() : null;
        } else if (currentViewMode == ViewMode.TEAM_HEATMAP) {
            startDate = LocalDate.of(selectedDate.getYear(), 1, 1);
            endDate = LocalDate.of(selectedDate.getYear(), 12, 31);
        } else {
            startDate = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = startDate.plusDays(6);
            employeeKey = (selectedEmployee != null) ? selectedEmployee.getEmployeeKey() : null;
        }

        Log.i(TAG, "Refresh Data: " + startDate + " bis " + endDate + " (Branch: " + branchId + ", Employee: " + employeeKey + ")");
        viewModel.refreshData(startDate, endDate, employeeKey, branchId);

        if (currentViewMode == ViewMode.TEAM_HEATMAP) {
            viewModel.fetchAllAbsences();
        }
    }

    private enum ViewMode {
        BRANCH, EMPLOYEE, ABSENCE, TEAM_HEATMAP
    }
}
