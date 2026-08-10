package de.mamakow.dienstplanapotheke.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class RosterEmployeeFragment extends Fragment {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN);
    private MainViewModel viewModel;
    private RosterAdapter rosterAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View progressBar;
    private Spinner employeeSpinner;
    private Button buttonDatePicker;
    private ImageButton buttonPrevDate;
    private ImageButton buttonNextDate;
    private List<Employee> availableEmployees = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roster_employee, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        employeeSpinner = view.findViewById(R.id.employeeSpinner);
        buttonDatePicker = view.findViewById(R.id.buttonDatePicker);
        buttonPrevDate = view.findViewById(R.id.buttonPrevDate);
        buttonNextDate = view.findViewById(R.id.buttonNextDate);

        rosterAdapter = new RosterAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(rosterAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupDateNavigation();
        setupEmployeeSpinner();
        setupObservers();

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void setupDateNavigation() {
        buttonPrevDate.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current != null) {
                viewModel.setSelectedDate(current.minusWeeks(1));
            }
        });

        buttonNextDate.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current != null) {
                viewModel.setSelectedDate(current.plusWeeks(1));
            }
        });

        buttonDatePicker.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current == null) current = LocalDate.now();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                viewModel.setSelectedDate(picked);
            }, current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()).show();
        });
    }

    private void setupEmployeeSpinner() {
        employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < availableEmployees.size()) {
                    Employee selected = availableEmployees.get(position);
                    Employee current = viewModel.getSelectedEmployee().getValue();
                    if (current == null || current.getEmployeeKey() != selected.getEmployeeKey()) {
                        viewModel.setSelectedEmployee(selected);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupObservers() {
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                buttonDatePicker.setText(getString(R.string.woche_vom, date.format(dateFormatter)));
                refreshData();
            }
        });

        viewModel.getSelectedEmployee().observe(getViewLifecycleOwner(), employee -> {
            if (employee != null) {
                updateSpinnerSelection(employee);
                refreshData();
            }
        });

        viewModel.getWorkforce().observe(getViewLifecycleOwner(), workforce -> {
            if (workforce != null) {
                availableEmployees = workforce.getEmployees();
                rosterAdapter.setEmployees(availableEmployees);
                updateSpinnerAdapter(workforce.getEmployeeNames());
                Employee current = viewModel.getSelectedEmployee().getValue();
                if (current != null) {
                    updateSpinnerSelection(current);
                }
            }
        });

        viewModel.getRoster().observe(getViewLifecycleOwner(), roster -> {
            if (roster != null) {
                rosterAdapter.setRosterDays(roster.getRosterDays());
            }
        });

        viewModel.getBranches().observe(getViewLifecycleOwner(), branches -> {
            if (branches != null) {
                rosterAdapter.setBranches(branches);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateSpinnerAdapter(List<String> names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(adapter);
    }

    private void updateSpinnerSelection(Employee employee) {
        for (int i = 0; i < availableEmployees.size(); i++) {
            if (availableEmployees.get(i).getEmployeeKey() == employee.getEmployeeKey()) {
                employeeSpinner.setSelection(i);
                break;
            }
        }
    }

    private void refreshData() {
        LocalDate startDate = viewModel.getSelectedDate().getValue();
        Employee employee = viewModel.getSelectedEmployee().getValue();
        if (startDate != null && employee != null) {
            LocalDate endDate = startDate.plusDays(6);
            viewModel.refreshData(startDate, endDate, employee.getEmployeeKey(), null);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
