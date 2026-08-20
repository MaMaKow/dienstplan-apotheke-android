package de.mamakow.dienstplanapotheke.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Absence;

public class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.AbsenceViewHolder> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private List<Absence> absences = new ArrayList<>();

    public void setAbsences(List<Absence> newAbsences) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AbsenceDiffCallback(this.absences, newAbsences));
        this.absences = new ArrayList<>(newAbsences);
        diffResult.dispatchUpdatesTo(this);
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

        String start = absence.getStartDate().format(formatter);
        String end = absence.getEndDate().format(formatter);
        holder.tvAbsenceDate.setText(holder.itemView.getContext().getString(R.string.date_range_format, start, end));

        holder.tvAbsenceComment.setText(absence.getComment());
    }

    @Override
    public int getItemCount() {
        return absences.size();
    }

    private static class AbsenceDiffCallback extends DiffUtil.Callback {
        private final List<Absence> oldList;
        private final List<Absence> newList;

        AbsenceDiffCallback(List<Absence> oldList, List<Absence> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
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
