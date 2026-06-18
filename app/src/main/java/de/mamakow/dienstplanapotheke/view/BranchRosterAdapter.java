package de.mamakow.dienstplanapotheke.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.model.Workforce;

public class BranchRosterAdapter extends RecyclerView.Adapter<BranchRosterAdapter.RosterViewHolder> {

    private final Map<Integer, Employee> employeeMap = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private List<RosterDay> rosterDays = new ArrayList<>();

    public BranchRosterAdapter() {
    }

    public void setRosterDays(List<RosterDay> rosterDays) {
        this.rosterDays = rosterDays;
        notifyDataSetChanged();
    }

    public void setEmployees(Workforce workforce) {
        employeeMap.clear();
        if (workforce != null) {
            for (Employee e : workforce.getEmployees()) {
                employeeMap.put(e.getEmployeeKey(), e);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roster_day_branch, parent, false);
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
        private final LinearLayout layoutRosterItems;

        public RosterViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRosterItems = itemView.findViewById(R.id.layoutRosterItems);
        }

        public void bind(RosterDay rosterDay) {
            layoutRosterItems.removeAllViews();

            for (RosterItem item : rosterDay.getRosterItems()) {
                View subItemView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_roster_shift_branch, layoutRosterItems, false);

                TextView textViewEmployeeName = subItemView.findViewById(R.id.textViewEmployeeName);
                TextView textViewShiftTime = subItemView.findViewById(R.id.textViewShiftTime);
                TextView textViewPause = subItemView.findViewById(R.id.textViewPause);
                TextView textViewComment = subItemView.findViewById(R.id.textViewComment);

                // Mitarbeiter-Name
                Employee employee = employeeMap.get(item.getEmployeeKey());
                String name = (employee != null) ? employee.getEmployeeFullName() : "Unbekannt (" + item.getEmployeeKey() + ")";
                textViewEmployeeName.setText(name);

                // Schichtzeit
                String shiftTime = item.getDutyStartDateTime().format(timeFormatter) + " - " + item.getDutyEndDateTime().format(timeFormatter);
                textViewShiftTime.setText(shiftTime);

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
                MaterialCardView cardView = (MaterialCardView) subItemView;

                if (employee != null) {
                    String profession = employee.getEmployeeProfession();
                    int backgroundColor = android.graphics.Color.WHITE; // Standard
                    int fontColor = itemView.getContext().getColor(R.color.md_theme_onSurface);

                    if ("Apotheker".equalsIgnoreCase(profession)) {
                        backgroundColor = itemView.getContext().getColor(R.color.md_theme_primaryContainer);
                        fontColor = itemView.getContext().getColor(R.color.md_theme_onPrimaryContainer);
                        cardView.setStrokeWidth(4); // Stärkere Umrandung
                    } else if ("Pharmazieingenieur".equalsIgnoreCase(profession) || "PI".equalsIgnoreCase(profession)) {
                        backgroundColor = itemView.getContext().getColor(R.color.md_theme_secondaryContainer);
                        fontColor = itemView.getContext().getColor(R.color.md_theme_onSecondaryContainer);
                        cardView.setStrokeWidth(4);
                    } else if ("PTA".equalsIgnoreCase(profession)) {
                        backgroundColor = itemView.getContext().getColor(R.color.md_theme_tertiaryContainer);
                        fontColor = itemView.getContext().getColor(R.color.md_theme_onTertiaryContainer);

                        cardView.setStrokeWidth(1);
                    } else {
                        // Nichtpharmazeutisches Personal:
                        cardView.setStrokeWidth(1);
                    }
                    // 1. Set the background of the card
                    cardView.setCardBackgroundColor(backgroundColor);

                    // 2. Set the text color of each individual TextView
                    textViewEmployeeName.setTextColor(fontColor);
                    textViewShiftTime.setTextColor(fontColor);
                    textViewPause.setTextColor(fontColor);
                    textViewComment.setTextColor(fontColor);
                }
                layoutRosterItems.addView(subItemView);
            }
        }
    }
}
