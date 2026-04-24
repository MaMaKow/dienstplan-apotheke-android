package de.mamakow.dienstplanapotheke.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.AbsenceDayData;
import de.mamakow.dienstplanapotheke.model.AbsenceWithName;

public class AbsenceDetailBottomSheet extends BottomSheetDialogFragment {

    private final LocalDate date;
    private final AbsenceDayData data;

    public AbsenceDetailBottomSheet(LocalDate date, AbsenceDayData data) {
        this.date = date;
        this.data = data;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_absence_details, container, false);

        TextView title = view.findViewById(R.id.sheet_title);
        title.setText(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));

        RecyclerView recyclerView = view.findViewById(R.id.absence_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new DetailAdapter(data.getAbsences()));

        return view;
    }

    private static class DetailAdapter extends RecyclerView.Adapter<DetailViewHolder> {
        private final List<AbsenceWithName> absences;

        DetailAdapter(List<AbsenceWithName> absences) {
            this.absences = absences;
        }

        @NonNull
        @Override
        public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_absence_detail_row, parent, false);
            return new DetailViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
            holder.bind(absences.get(position));
        }

        @Override
        public int getItemCount() {
            return absences.size();
        }
    }

    private static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView name, reason, status, comment;

        DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.detail_name);
            reason = itemView.findViewById(R.id.detail_reason);
            status = itemView.findViewById(R.id.detail_status);
            comment = itemView.findViewById(R.id.detail_comment);
        }

        void bind(AbsenceWithName item) {
            name.setText(item.getEmployeeName() + " (" + item.getProfession() + ")");
            reason.setText(item.getAbsence().getAbsenceTypeString());
            status.setText("Status: " + (item.getAbsence().getComment() != null ? "Genehmigt" : "Offen")); // Platzhalter Logik
            comment.setText(item.getAbsence().getComment());
            comment.setVisibility(item.getAbsence().getComment() != null && !item.getAbsence().getComment().isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
