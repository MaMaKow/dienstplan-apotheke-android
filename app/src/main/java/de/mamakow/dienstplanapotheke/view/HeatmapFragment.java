package de.mamakow.dienstplanapotheke.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.AbsenceDayData;
import de.mamakow.dienstplanapotheke.viewmodel.HeatmapViewModel;

public class HeatmapFragment extends Fragment implements HeatmapAdapter.OnDayClickListener {

    private HeatmapViewModel viewModel;
    private HeatmapAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_heatmap, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.heatmap_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HeatmapAdapter(this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(HeatmapViewModel.class);
        viewModel.getHeatmapData().observe(getViewLifecycleOwner(), monthData -> {
            adapter.submitList(monthData);
        });

        return view;
    }

    @Override
    public void onDayClick(LocalDate date, AbsenceDayData data) {
        if (data != null && !data.getAbsences().isEmpty()) {
            AbsenceDetailBottomSheet bottomSheet = new AbsenceDetailBottomSheet(date, data);
            bottomSheet.show(getChildFragmentManager(), "AbsenceDetail");
        }
    }

    @Override
    public void onDayLongClick(LocalDate date, AbsenceDayData data, View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenu().add("Genehmigen");
        popup.getMenu().add("Bearbeiten");
        popup.getMenu().add("Ablehnen");

        popup.setOnMenuItemClickListener(item -> {
            Toast.makeText(getContext(), item.getTitle() + " für " + date, Toast.LENGTH_SHORT).show();
            return true;
        });
        popup.show();
    }
}
