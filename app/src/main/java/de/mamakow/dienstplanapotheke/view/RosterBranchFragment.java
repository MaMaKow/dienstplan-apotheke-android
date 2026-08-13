package de.mamakow.dienstplanapotheke.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class RosterBranchFragment extends Fragment {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy", Locale.GERMAN);
    private MainViewModel viewModel;
    private BranchRosterAdapter branchRosterAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View progressBar;
    private AutoCompleteTextView branchSpinner;
    private MaterialButton buttonDatePicker;
    private MaterialButton buttonPrevDate;
    private MaterialButton buttonNextDate;
    private View emptyStateView;
    private List<Branch> availableBranches = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roster_branch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        branchSpinner = view.findViewById(R.id.branchSpinner);
        buttonDatePicker = view.findViewById(R.id.buttonDatePicker);
        buttonPrevDate = view.findViewById(R.id.buttonPrevDate);
        buttonNextDate = view.findViewById(R.id.buttonNextDate);
        emptyStateView = view.findViewById(R.id.emptyStateView);

        branchRosterAdapter = new BranchRosterAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(branchRosterAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupDateNavigation();
        setupBranchSpinner();
        setupObservers();

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void setupDateNavigation() {
        buttonPrevDate.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current != null) {
                viewModel.setSelectedDate(current.minusDays(1));
            }
        });

        buttonNextDate.setOnClickListener(v -> {
            LocalDate current = viewModel.getSelectedDate().getValue();
            if (current != null) {
                viewModel.setSelectedDate(current.plusDays(1));
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

    private void setupBranchSpinner() {
        branchSpinner.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < availableBranches.size()) {
                Branch selectedBranch = availableBranches.get(position);
                Branch currentBranch = viewModel.getSelectedBranch().getValue();

                if (currentBranch == null || currentBranch.getBranchId() != selectedBranch.getBranchId()) {
                    viewModel.setSelectedBranch(selectedBranch);
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                buttonDatePicker.setText(date.format(dateFormatter));
                refreshData();
            }
        });

        viewModel.getSelectedBranch().observe(getViewLifecycleOwner(), branch -> {
            if (branch != null) {
                updateSpinnerSelection(branch);
                refreshData();
            }
        });

        viewModel.getBranches().observe(getViewLifecycleOwner(), branches -> {
            if (branches != null && !branches.isEmpty()) {
                availableBranches = branches;
                updateSpinnerAdapter(branches.stream().map(Branch::getBranchName).collect(Collectors.toList()));

                Branch current = viewModel.getSelectedBranch().getValue();
                if (current == null) {
                    // Pre-select first branch if none is selected
                    viewModel.setSelectedBranch(branches.get(0));
                } else {
                    updateSpinnerSelection(current);
                }
            }
        });

        viewModel.getWorkforce().observe(getViewLifecycleOwner(), workforce -> {
            if (workforce != null) {
                branchRosterAdapter.setEmployees(workforce);
            }
        });

        viewModel.getRoster().observe(getViewLifecycleOwner(), roster -> {
            if (roster != null) {
                branchRosterAdapter.setRosterDays(roster.getRosterDays());
                boolean isEmpty = roster.getRosterDays().isEmpty();
                emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            } else {
                emptyStateView.setVisibility(View.VISIBLE);
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

    private void updateSpinnerAdapter(List<String> names) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, names);
        branchSpinner.setAdapter(adapter);
    }

    private void updateSpinnerSelection(Branch branch) {
        branchSpinner.setText(branch.getBranchName(), false);
    }

    private void refreshData() {
        LocalDate date = viewModel.getSelectedDate().getValue();
        Branch branch = viewModel.getSelectedBranch().getValue();
        if (date != null && branch != null) {
            viewModel.refreshData(date, date, null, branch.getBranchId());
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
