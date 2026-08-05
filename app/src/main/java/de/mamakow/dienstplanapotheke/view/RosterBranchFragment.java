package de.mamakow.dienstplanapotheke.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class RosterBranchFragment extends Fragment {

    private MainViewModel viewModel;
    private BranchRosterAdapter branchRosterAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View progressBar;
    private Spinner branchSpinner;
    private List<Branch> availableBranches = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roster_branch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        branchSpinner = view.findViewById(R.id.branchSpinner);

        branchRosterAdapter = new BranchRosterAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(branchRosterAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        setupBranchSpinner();
        setupObservers();

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void setupBranchSpinner() {
        branchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < availableBranches.size()) {
                    Branch selectedBranch = availableBranches.get(position);
                    Branch currentSelected = viewModel.getSelectedBranch().getValue();

                    // Only trigger refresh if the branch has actually changed.
                    // This prevents an infinite loop caused by programmatic selection changes
                    // (like setAdapter or setSelection) during spinner updates.
                    if (currentSelected == null || currentSelected.getBranchId() != selectedBranch.getBranchId()) {
                        viewModel.setSelectedBranch(selectedBranch);
                        refreshData();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupObservers() {
        viewModel.getBranches().observe(getViewLifecycleOwner(), branches -> {
            if (branches != null) {
                availableBranches = branches;
                updateSpinner(branches);
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

    private void updateSpinner(List<Branch> branches) {
        List<String> names = branches.stream().map(Branch::getBranchName).collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Option B: Suppress the listener during the update to prevent redundant calls
        branchSpinner.setOnItemSelectedListener(null);
        branchSpinner.setAdapter(adapter);

        // Restore selection if a branch was already selected in ViewModel
        Branch current = viewModel.getSelectedBranch().getValue();
        if (current != null) {
            for (int i = 0; i < branches.size(); i++) {
                if (branches.get(i).getBranchId() == current.getBranchId()) {
                    branchSpinner.setSelection(i);
                    break;
                }
            }
        }

        // Re-attach the listener
        setupBranchSpinner();
    }

    private void refreshData() {
        LocalDate now = LocalDate.now();
        Branch selectedBranch = viewModel.getSelectedBranch().getValue();
        Integer branchId = selectedBranch != null ? selectedBranch.getBranchId() : null;
        viewModel.refreshData(now, now, null, branchId);
    }
}
