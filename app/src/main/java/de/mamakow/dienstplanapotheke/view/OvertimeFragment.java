package de.mamakow.dienstplanapotheke.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class OvertimeFragment extends Fragment {

    private MainViewModel viewModel;
    private OvertimeAdapter overtimeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View progressBar;
    private AutoCompleteTextView employeeSpinner;
    private MaterialButton buttonDatePicker;
    private MaterialButton buttonPrevDate;
    private MaterialButton buttonNextDate;
    private View emptyStateView;
    private TextView emptyStateTextView;

    private List<Employee> availableEmployees = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_date_nav, container, false);
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
        emptyStateView = view.findViewById(R.id.emptyStateView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);

        // Customize empty state for Overtime
        emptyStateTextView.setText(R.string.keine_ueberstunden_gefunden);

        overtimeAdapter = new OvertimeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(overtimeAdapter);

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
                viewModel.setSelectedDate(current.minusYears(1));
            }
        });

        buttonNextDate.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current != null) {
                viewModel.setSelectedDate(current.plusYears(1));
            }
        });

        buttonDatePicker.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current == null) current = LocalDate.now();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
                viewModel.setSelectedDate(picked);
            }, current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()).show();
        });
    }

    private void setupEmployeeSpinner() {
        employeeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < availableEmployees.size()) {
                Employee selected = availableEmployees.get(position);
                Employee current = viewModel.getSelectedEmployee().getValue();
                if (current == null || current.getEmployeeKey() != selected.getEmployeeKey()) {
                    viewModel.setSelectedEmployee(selected);
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                buttonDatePicker.setText(String.valueOf(date.getYear()));
                observeOvertimes();
                refreshData();
            }
        });

        viewModel.getSelectedEmployee().observe(getViewLifecycleOwner(), employee -> {
            if (employee != null) {
                updateSpinnerSelection(employee);
                observeOvertimes();
                refreshData();
            }
        });

        viewModel.getWorkforce().observe(getViewLifecycleOwner(), workforce -> {
            if (workforce != null) {
                availableEmployees = workforce.getEmployees();
                updateSpinnerAdapter(workforce.getEmployeeNames());
                Employee current = viewModel.getSelectedEmployee().getValue();
                if (current != null) {
                    updateSpinnerSelection(current);
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (isLoading) {
                emptyStateView.setVisibility(View.GONE);
            }
        });
    }

    private void observeOvertimes() {
        Employee employee = viewModel.getSelectedEmployee().getValue();
        LocalDate date = viewModel.getSelectedDate().getValue();
        if (employee != null && date != null) {
            viewModel.getOvertimesForEmployeeAndYear(employee.getEmployeeKey(), date.getYear()).removeObservers(getViewLifecycleOwner());
            viewModel.getOvertimesForEmployeeAndYear(employee.getEmployeeKey(), date.getYear()).observe(getViewLifecycleOwner(), overtimes -> {
                if (overtimes != null) {
                    overtimeAdapter.setOvertimes(overtimes);
                    emptyStateView.setVisibility(overtimes.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    emptyStateView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void updateSpinnerAdapter(List<String> names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, names);
        employeeSpinner.setAdapter(adapter);
    }

    private void updateSpinnerSelection(Employee employee) {
        for (int i = 0; i < availableEmployees.size(); i++) {
            if (availableEmployees.get(i).getEmployeeKey() == employee.getEmployeeKey()) {
                employeeSpinner.setText(availableEmployees.get(i).getEmployeeFullName(), false);
                break;
            }
        }
    }

    private void refreshData() {
        Employee employee = viewModel.getSelectedEmployee().getValue();
        if (employee != null) {
            viewModel.fetchOvertimes(employee.getEmployeeKey());
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
