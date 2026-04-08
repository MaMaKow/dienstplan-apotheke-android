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
import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.view.RosterAdapter;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
    private MainViewModel viewModel;
    private RosterAdapter adapter;
    private View filterLayout;
    private RadioGroup viewModeRadioGroup;
    private Button buttonDatePicker;
    private ImageButton buttonPrevDate;
    private ImageButton buttonNextDate;
    private Spinner branchSpinner;
    private TextView currentSelectionTextView;
    private LocalDate selectedDate;
    private Branch selectedBranch;
    private List<Branch> allBranches = new ArrayList<>();
    private boolean isBranchView = false;

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
        refreshRosterOnly();
    }

    private void initViews() {
        filterLayout = findViewById(R.id.filterLayout);
        viewModeRadioGroup = findViewById(R.id.viewModeRadioGroup);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        buttonPrevDate = findViewById(R.id.buttonPrevDate);
        buttonNextDate = findViewById(R.id.buttonNextDate);
        branchSpinner = findViewById(R.id.branchSpinner);
        currentSelectionTextView = findViewById(R.id.currentSelectionTextView);
    }

    private void setupSession() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewRoster);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RosterAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getEmployees().observe(this, employees -> {
            if (employees != null) {
                adapter.setEmployees(employees);
            }
        });

        viewModel.getBranches().observe(this, branches -> {
            if (branches != null && !branches.equals(allBranches)) {
                this.allBranches = branches;
                adapter.setBranches(branches);
                updateBranchSpinner();
            }
        });

        // WICHTIG: Wir nutzen jetzt getRoster() ohne Filter, 
        // da die Repository-DB-Abfrage gefixt wurde.
        viewModel.getRoster().observe(this, roster -> {
            if (roster != null) {
                Log.d(TAG, "Dienstplan-Update: " + roster.getRosterDays().size() + " Tage angezeigt.");
                adapter.setRosterDays(roster.getRosterDays());
            }
        });
    }

    private void setupListeners() {
        viewModeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isBranchView = (checkedId == R.id.radioBranchView);
            filterLayout.setVisibility(isBranchView ? View.VISIBLE : View.GONE);
            updateUI();
            refreshRosterOnly();
        });

        buttonDatePicker.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                updateUI();
                refreshRosterOnly();
            }, selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
            datePicker.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
            datePicker.show();
        });

        buttonPrevDate.setOnClickListener(v -> {
            if (isBranchView) {
                selectedDate = selectedDate.minusDays(1);
            } else {
                selectedDate = selectedDate.minusWeeks(1);
            }
            updateUI();
            refreshRosterOnly();
        });

        buttonNextDate.setOnClickListener(v -> {
            if (isBranchView) {
                selectedDate = selectedDate.plusDays(1);
            } else {
                selectedDate = selectedDate.plusWeeks(1);
            }
            updateUI();
            refreshRosterOnly();
        });

        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Branch newSelection = allBranches.get(position);
                if (selectedBranch == null || selectedBranch.getBranchId() != newSelection.getBranchId()) {
                    selectedBranch = newSelection;
                    updateUI();
                    refreshRosterOnly();
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

        branchSpinner.setVisibility(allBranches.size() > 1 ? View.VISIBLE : View.GONE);

        if (selectedBranch == null) {
            selectedBranch = allBranches.get(0);
        }
    }

    private void updateUI() {
        if (isBranchView) {
            String branchName = (selectedBranch != null) ? selectedBranch.getBranchName() : "...";
            currentSelectionTextView.setText("Tagesansicht: " + selectedDate.format(dateFormatter) + " - " + branchName);
            buttonDatePicker.setText(selectedDate.format(dateFormatter));
        } else {
            LocalDate monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = monday.plusDays(6);
            currentSelectionTextView.setText("Wochenansicht Mitarbeiter: " + monday.format(dateFormatter) + " - " + sunday.format(dateFormatter));
            buttonDatePicker.setText("Woche vom " + monday.format(dateFormatter));
        }
    }

    private void refreshRosterOnly() {
        LocalDate startDate, endDate;
        Integer employeeKey = null;
        Integer branchId = null;

        if (isBranchView) {
            startDate = selectedDate;
            endDate = selectedDate;
            branchId = (selectedBranch != null) ? selectedBranch.getBranchId() : null;
        } else {
            startDate = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = startDate.plusDays(6);
            employeeKey = 7;
        }

        Log.i(TAG, "Refresh Roster: " + startDate + " bis " + endDate + " (Branch: " + branchId + ")");
        viewModel.refreshData(startDate, endDate, employeeKey, branchId);
    }
}
