package de.mamakow.dienstplanapotheke;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.view.AbsenceAdapter;
import de.mamakow.dienstplanapotheke.view.RosterAdapter;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
    private MainViewModel viewModel;
    private RecyclerView recyclerView;
    private RosterAdapter rosterAdapter;
    private AbsenceAdapter absenceAdapter;
    private RadioGroup viewModeRadioGroup;
    private Button buttonDatePicker;
    private ImageButton buttonPrevDate;
    private ImageButton buttonNextDate;
    private Spinner branchSpinner;
    private Spinner employeeSpinner;

    private TextView currentSelectionTextView;
    private LocalDate selectedDate;
    private Branch selectedBranch;
    private Employee selectedEmployee;

    private List<Branch> allBranches = new ArrayList<>();
    private List<Employee> employees = new ArrayList<>();
    private ViewMode currentViewMode = ViewMode.EMPLOYEE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupSession();
        setupRecyclerView();

        // Initiales Datum: Heute oder nächster Montag
        selectedDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        setupViewModel();
        setupListeners();

        // Stammdaten und initialen Roster laden
        updateUI();
        refreshData();
    }

    private void initViews() {
        viewModeRadioGroup = findViewById(R.id.viewModeRadioGroup);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        buttonPrevDate = findViewById(R.id.buttonPrevDate);
        buttonNextDate = findViewById(R.id.buttonNextDate);
        branchSpinner = findViewById(R.id.branchSpinner);
        employeeSpinner = findViewById(R.id.employeeSpinner);
        currentSelectionTextView = findViewById(R.id.currentSelectionTextView);
        recyclerView = findViewById(R.id.recyclerViewRoster);
    }

    private void setupSession() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rosterAdapter = new RosterAdapter();
        absenceAdapter = new AbsenceAdapter();
        recyclerView.setAdapter(rosterAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getEmployees().observe(this, employees -> {
            if (employees != null) {
                this.employees = employees;
                rosterAdapter.setEmployees(employees);
                updateEmployeeSpinner();
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
            if (roster != null && currentViewMode != ViewMode.ABSENCE) {
                Log.d(TAG, "Dienstplan-Update: " + roster.getRosterDays().size() + " Tage angezeigt.");
                rosterAdapter.setRosterDays(roster.getRosterDays());
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
        viewModeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBranchView) {
                currentViewMode = ViewMode.BRANCH;
                recyclerView.setAdapter(rosterAdapter);
            } else if (checkedId == R.id.radioAbsenceView) {
                currentViewMode = ViewMode.ABSENCE;
                recyclerView.setAdapter(absenceAdapter);
                observeAbsences();
            } else {
                currentViewMode = ViewMode.EMPLOYEE;
                recyclerView.setAdapter(rosterAdapter);
            }
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
            } else if (currentViewMode == ViewMode.ABSENCE) {
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
            } else if (currentViewMode == ViewMode.ABSENCE) {
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
                Employee newSelection = employees.get(position);
                if (selectedEmployee == null || selectedEmployee.getEmployeeKey() != newSelection.getEmployeeKey()) {
                    selectedEmployee = newSelection;
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

    private void updateEmployeeSpinner() {
        if (employees.isEmpty()) return;

        int selectedIndex = 0;
        List<String> employeeNames = new ArrayList<>();
        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            employeeNames.add(employee.getEmployeeFullName());
            if (selectedEmployee != null && employee.getEmployeeKey() == selectedEmployee.getEmployeeKey()) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, employeeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(spinnerAdapter);
        employeeSpinner.setSelection(selectedIndex);

        if (selectedEmployee == null) {
            selectedEmployee = employees.get(0);
        }
    }

    private void updateUI() {
        if (currentViewMode == ViewMode.BRANCH) {
            currentSelectionTextView.setText(String.format("%s%s", getString(R.string.tagesansicht), selectedDate.format(dateFormatter)));
            buttonDatePicker.setText(selectedDate.format(dateFormatter));
            branchSpinner.setVisibility(View.VISIBLE);
            employeeSpinner.setVisibility(View.GONE);
        } else if (currentViewMode == ViewMode.ABSENCE) {
            currentSelectionTextView.setText(String.format("%s %d", getString(R.string.jahresansicht_abwesenheiten), selectedDate.getYear()));
            buttonDatePicker.setText(String.valueOf(selectedDate.getYear()));
            branchSpinner.setVisibility(View.GONE);
            employeeSpinner.setVisibility(View.VISIBLE);
        } else {
            LocalDate monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = monday.plusDays(6);
            currentSelectionTextView.setText(String.format("%s%s - %s", getString(R.string.wochenansicht_mitarbeiter), monday.format(dateFormatter), sunday.format(dateFormatter)));
            buttonDatePicker.setText(String.format("%s%s", getString(R.string.woche_vom), monday.format(dateFormatter)));
            branchSpinner.setVisibility(View.GONE);
            employeeSpinner.setVisibility(View.VISIBLE);
        }
    }

    private void refreshData() {
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
        } else {
            startDate = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = startDate.plusDays(6);
            employeeKey = (selectedEmployee != null) ? selectedEmployee.getEmployeeKey() : null;
        }

        Log.i(TAG, "Refresh Data: " + startDate + " bis " + endDate + " (Branch: " + branchId + ", Employee: " + employeeKey + ")");
        viewModel.refreshData(startDate, endDate, employeeKey, branchId);
    }

    private enum ViewMode {
        BRANCH, EMPLOYEE, ABSENCE
    }
}
