package de.mamakow.dienstplanapotheke.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;

public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.RosterViewHolder> {

    private final Map<Integer, Employee> employeeMap = new HashMap<>();
    private final Map<Integer, Branch> branchMap = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private List<RosterDay> rosterDays = new ArrayList<>();

    public RosterAdapter() {
    }

    public void setRosterDays(List<RosterDay> rosterDays) {
        this.rosterDays = rosterDays;
        notifyDataSetChanged();
    }

    public void setEmployees(List<Employee> employees) {
        employeeMap.clear();
        if (employees != null) {
            for (Employee e : employees) {
                employeeMap.put(e.getEmployeeKey(), e);
            }
        }
        notifyDataSetChanged();
    }

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

    class RosterViewHolder extends RecyclerView.ViewHolder {
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
                View subItemView = LayoutInflater.from(itemView.getContext()).inflate(android.R.layout.simple_list_item_2, layoutRosterItems, false);
                TextView text1 = subItemView.findViewById(android.R.id.text1);
                TextView text2 = subItemView.findViewById(android.R.id.text2);

                Employee employee = employeeMap.get(item.getEmployeeKey());
                String name = (employee != null) ? employee.getEmployeeFullName() : "Unbekannt (" + item.getEmployeeKey() + ")";

                text1.setText(name);

                String detailedInformation = item.getDutyStartDateTime().format(timeFormatter) + " - " + item.getDutyEndDateTime().format(timeFormatter);
                if (item.getBreakStartDateTime() != null && item.getBreakEndDateTime() != null) {
                    detailedInformation += "\nPause: " + item.getBreakStartDateTime().format(timeFormatter) + " - " + item.getBreakEndDateTime().format(timeFormatter);
                }
                if (item.getComment() != null && !item.getComment().isEmpty()) {
                    detailedInformation += "\n(" + item.getComment() + ")";
                }

                int branchId = item.getBranchId();
                Branch branch = branchMap.get(branchId);
                String branchName = (branch != null) ? branch.getBranchName() : "Unbekannt (" + branchId + ")";
                detailedInformation += "\nFiliale: " + branchName;
                text2.setText(detailedInformation);

                layoutRosterItems.addView(subItemView, layoutRosterItems.getChildCount());
            }
        }
    }
}
