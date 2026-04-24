package de.mamakow.dienstplanapotheke.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Absence;

public class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.AbsenceViewHolder> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private List<Absence> absences = new ArrayList<>();

    public void setAbsences(List<Absence> absences) {
        this.absences = absences;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AbsenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_absence, parent, false);
        return new AbsenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsenceViewHolder holder, int position) {
        Absence absence = absences.get(position);
        holder.tvAbsenceType.setText(absence.getAbsenceTypeString());
        String dateRange = absence.getStartDate().format(formatter) + " - " + absence.getEndDate().format(formatter);
        holder.tvAbsenceDate.setText(dateRange);
        holder.tvAbsenceComment.setText(absence.getComment());
    }

    @Override
    public int getItemCount() {
        return absences.size();
    }

    static class AbsenceViewHolder extends RecyclerView.ViewHolder {
        TextView tvAbsenceType;
        TextView tvAbsenceDate;
        TextView tvAbsenceComment;

        public AbsenceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAbsenceType = itemView.findViewById(R.id.tvAbsenceType);
            tvAbsenceDate = itemView.findViewById(R.id.tvAbsenceDate);
            tvAbsenceComment = itemView.findViewById(R.id.tvAbsenceComment);
        }
    }
}
