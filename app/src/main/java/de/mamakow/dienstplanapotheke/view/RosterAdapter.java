package de.mamakow.dienstplanapotheke.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;

public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.RosterViewHolder> {

    private final Map<Integer, Branch> branchMap = new HashMap<>();
    private final Map<Integer, Employee> employeeMap = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private List<RosterDay> rosterDays = new ArrayList<>();

    public RosterAdapter() {
    }

    public void setRosterDays(List<RosterDay> newRosterDays) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RosterDiffCallback(this.rosterDays, newRosterDays));
        this.rosterDays = new ArrayList<>(newRosterDays);
        diffResult.dispatchUpdatesTo(this);
    }

    @SuppressWarnings("NotifyDataSetChanged")
    public void setEmployees(List<Employee> employeeList) {
        employeeMap.clear();
        if (employeeList != null) {
            for (Employee employee : employeeList) {
                employeeMap.put(employee.getEmployeeKey(), employee);
            }
        }
        notifyDataSetChanged();
    }

    @SuppressWarnings("NotifyDataSetChanged")
    public void setBranches(List<Branch> branches) {
        branchMap.clear();
        if (branches != null) {
            for (Branch b : branches) {
                branchMap.put(b.getBranchId(), b);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roster_day, parent, false);
        return new RosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RosterViewHolder holder, int position) {
        RosterDay rosterDay = rosterDays.get(position);
        holder.bind(rosterDay);
    }

    @Override
    public int getItemCount() {
        return rosterDays.size();
    }

    private static class RosterDiffCallback extends DiffUtil.Callback {
        private final List<RosterDay> oldList;
        private final List<RosterDay> newList;

        RosterDiffCallback(List<RosterDay> oldList, List<RosterDay> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getLocalDate().equals(newList.get(newItemPosition).getLocalDate());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            RosterDay oldDay = oldList.get(oldItemPosition);
            RosterDay newDay = newList.get(newItemPosition);

            List<RosterItem> oldItems = oldDay.getRosterItems();
            List<RosterItem> newItems = newDay.getRosterItems();

            if (oldItems.size() != newItems.size()) return false;

            for (int i = 0; i < oldItems.size(); i++) {
                RosterItem oldItem = oldItems.get(i);
                RosterItem newItem = newItems.get(i);

                if (oldItem.getBranchId() != newItem.getBranchId()) return false;
                if (!Objects.equals(oldItem.getDutyStartDateTime(), newItem.getDutyStartDateTime()))
                    return false;
                if (!Objects.equals(oldItem.getDutyEndDateTime(), newItem.getDutyEndDateTime()))
                    return false;
                if (oldItem.getEmployeeKey() != newItem.getEmployeeKey()) return false;
                if (!Objects.equals(oldItem.getComment(), newItem.getComment())) return false;
                if (!Objects.equals(oldItem.getBreakStartDateTime(), newItem.getBreakStartDateTime()))
                    return false;
                if (!Objects.equals(oldItem.getBreakEndDateTime(), newItem.getBreakEndDateTime()))
                    return false;
            }

            return true;
        }
    }

    public class RosterViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewDate;
        private final LinearLayout layoutRosterItems;

        public RosterViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            layoutRosterItems = itemView.findViewById(R.id.layoutRosterItems);
        }

        public void bind(RosterDay rosterDay) {
            textViewDate.setText(rosterDay.getLocalDate().format(dateFormatter));
            layoutRosterItems.removeAllViews();

            for (RosterItem item : rosterDay.getRosterItems()) {
                View subItemView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_roster_shift, layoutRosterItems, false);

                TextView textViewShiftTime = subItemView.findViewById(R.id.textViewShiftTime);
                TextView textViewBranch = subItemView.findViewById(R.id.textViewBranch);
                TextView textViewPause = subItemView.findViewById(R.id.textViewPause);
                TextView textViewComment = subItemView.findViewById(R.id.textViewComment);

                // Schichtzeit
                String shiftTime = item.getDutyStartDateTime().format(timeFormatter) + " - " + item.getDutyEndDateTime().format(timeFormatter);
                textViewShiftTime.setText(shiftTime);

                // Filiale
                int employeeKey = item.getEmployeeKey();
                Employee employee = employeeMap.get(employeeKey);
                int branchId = item.getBranchId();

                boolean isDefaultBranch = false;
                if (employee != null && employee.getEmployeeBranchId() != null) {
                    isDefaultBranch = (branchId == employee.getEmployeeBranchId());
                }

                if (!isDefaultBranch) {
                    Branch branch = branchMap.get(branchId);
                    String branchName = (branch != null) ? branch.getBranchName() : "Unbekannt (" + branchId + ")";
                    textViewBranch.setText(itemView.getContext().getString(R.string.filiale_format, branchName));
                    textViewBranch.setVisibility(View.VISIBLE);
                } else {
                    textViewBranch.setVisibility(View.GONE);
                }

                // Pause (falls vorhanden)
                if (item.getBreakStartDateTime() != null && item.getBreakEndDateTime() != null) {
                    String pauseText = "Pause: " + item.getBreakStartDateTime().format(timeFormatter) + " - " + item.getBreakEndDateTime().format(timeFormatter);
                    textViewPause.setText(pauseText);
                    textViewPause.setVisibility(View.VISIBLE);
                } else {
                    textViewPause.setVisibility(View.GONE);
                }

                // Kommentar (falls vorhanden)
                if (item.getComment() != null && !item.getComment().isEmpty()) {
                    textViewComment.setText(item.getComment());
                    textViewComment.setVisibility(View.VISIBLE);
                } else {
                    textViewComment.setVisibility(View.GONE);
                }
                layoutRosterItems.addView(subItemView);
            }
        }
    }
}
