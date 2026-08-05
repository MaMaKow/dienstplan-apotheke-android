package de.mamakow.dienstplanapotheke.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.time.LocalDate;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class OvertimeFragment extends Fragment {

    private MainViewModel viewModel;
    private OvertimeAdapter overtimeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);

        overtimeAdapter = new OvertimeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(overtimeAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupObservers();

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Initial data load
        refreshData();
    }

    private void setupObservers() {
        SessionManager sessionManager = new SessionManager(requireContext());
        int employeeKey = sessionManager.getUserEmployeeKey();
        int year = LocalDate.now().getYear();

        if (employeeKey != -1) {
            viewModel.getOvertimesForEmployeeAndYear(employeeKey, year).observe(getViewLifecycleOwner(), overtimes -> {
                if (overtimes != null) {
                    overtimeAdapter.setOvertimes(overtimes);
                }
            });
        }

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (!swipeRefreshLayout.isRefreshing()) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshData() {
        SessionManager sessionManager = new SessionManager(requireContext());
        int employeeKey = sessionManager.getUserEmployeeKey();
        if (employeeKey != -1) {
            viewModel.fetchOvertimes(employeeKey);
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
