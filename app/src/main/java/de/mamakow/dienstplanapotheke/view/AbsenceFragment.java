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

public class AbsenceFragment extends Fragment {

    private MainViewModel viewModel;
    private AbsenceAdapter absenceAdapter;
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

        emptyStateTextView.setText(R.string.keine_abwesenheiten_gefunden);

        absenceAdapter = new AbsenceAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(absenceAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupDateNavigation();
        setupEmployeeSpinner();
        setupObservers();

        // Swipe-to-Refresh erzwingt einen API-Abgleich (Force Refresh)
        swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshData(true));
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
            new DatePickerDialog(requireContext(), (datePicker, year, month, dayOfMonth) -> {
                LocalDate picked = LocalDate.of(year, month + 1, dayOfMonth);
                viewModel.setSelectedDate(picked);
            }, current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth()).show();
        });
    }

    private void setupEmployeeSpinner() {
        employeeSpinner.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < availableEmployees.size()) {
                viewModel.setSelectedEmployee(availableEmployees.get(position));
            }
        });
    }

    private void setupObservers() {
        // Reaktive Beobachtung der Abwesenheiten vom ViewModel
        viewModel.getAbsences().observe(getViewLifecycleOwner(), absences -> {
            if (absences != null) {
                absenceAdapter.setAbsences(absences);
                emptyStateView.setVisibility(absences.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                emptyStateView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                buttonDatePicker.setText(String.valueOf(date.getYear()));
                viewModel.refreshData(false); // Automatischer Sync nur bei abgelaufenem Intervall
            }
        });

        viewModel.getSelectedEmployee().observe(getViewLifecycleOwner(), employee -> {
            if (employee != null) {
                updateSpinnerSelection(employee);
                viewModel.refreshData(false);
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
        });
    }

    private void updateSpinnerAdapter(List<String> names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, names);
        employeeSpinner.setAdapter(adapter);
    }

    private void updateSpinnerSelection(Employee employee) {
        for (Employee e : availableEmployees) {
            if (e.getEmployeeKey() == employee.getEmployeeKey()) {
                employeeSpinner.setText(e.getEmployeeFullName(), false);
                break;
            }
        }
    }
}
