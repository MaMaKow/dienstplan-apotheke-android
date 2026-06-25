package de.mamakow.dienstplanapotheke.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Overtime;

public class OvertimeAdapter extends RecyclerView.Adapter<OvertimeAdapter.OvertimeViewHolder> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private List<Overtime> overtimes = new ArrayList<>();

    public void setOvertimes(List<Overtime> overtimes) {
        this.overtimes = overtimes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OvertimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_overtime, parent, false);
        return new OvertimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OvertimeViewHolder holder, int position) {
        Overtime overtime = overtimes.get(position);
        // Balance:
        holder.tvOvertimeBalance.setText(String.format(Locale.GERMANY, "%.2f h", overtime.getBalance()));
        if (overtime.getBalance() >= 0) {
            holder.tvOvertimeBalance.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_primary));
        } else {
            holder.tvOvertimeBalance.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_error));
        }
        // Hours:
        holder.tvOvertimeHours.setText(String.format(Locale.GERMANY, "%+.1f h", overtime.getHours()));
        if (overtime.getHours() >= 0) {
            holder.tvOvertimeHours.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_primary));
        } else {
            holder.tvOvertimeHours.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.md_theme_error));
        }
        // Date:
        holder.tvOvertimeDate.setText(overtime.getDate().format(formatter));
        // Reason:
        holder.tvOvertimeReason.setText(overtime.getReason());
    }

    @Override
    public int getItemCount() {
        return overtimes.size();
    }

    static class OvertimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvOvertimeBalance;
        TextView tvOvertimeHours;
        TextView tvOvertimeDate;
        TextView tvOvertimeReason;

        public OvertimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOvertimeHours = itemView.findViewById(R.id.tvOvertimeHours);
            tvOvertimeBalance = itemView.findViewById(R.id.tvOvertimeBalance);
            tvOvertimeDate = itemView.findViewById(R.id.tvOvertimeDate);
            tvOvertimeReason = itemView.findViewById(R.id.tvOvertimeReason);
        }
    }
}
