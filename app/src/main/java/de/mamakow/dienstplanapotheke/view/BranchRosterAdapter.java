package de.mamakow.dienstplanapotheke.view;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.model.Workforce;
import de.mamakow.dienstplanapotheke.util.ColorUtils;

public class BranchRosterAdapter extends RecyclerView.Adapter<BranchRosterAdapter.RosterViewHolder> {

    private static final String TAG = "BranchRosterAdapter";
    private final Map<Integer, Employee> employeeMap = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN);
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private List<RosterDay> rosterDays = new ArrayList<>();

    public BranchRosterAdapter() {
    }

    public void setRosterDays(List<RosterDay> newRosterDays) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RosterDiffCallback(this.rosterDays, newRosterDays));
        this.rosterDays = new ArrayList<>(newRosterDays != null ? newRosterDays : new ArrayList<>());
        diffResult.dispatchUpdatesTo(this);
    }

    @SuppressWarnings("NotifyDataSetChanged")
    public void setEmployees(Workforce workforce) {
        employeeMap.clear();
        if (workforce != null && workforce.getEmployees() != null) {
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

    private static class RosterDiffCallback extends DiffUtil.Callback {
        private final List<RosterDay> oldList;
        private final List<RosterDay> newList;

        RosterDiffCallback(List<RosterDay> oldList, List<RosterDay> newList) {
            this.oldList = oldList;
            this.newList = newList != null ? newList : new ArrayList<>();
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
            return oldList.get(oldItemPosition).getLocalDate().equals(newList.get(newItemPosition).getLocalDate());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            RosterDay oldDay = oldList.get(oldItemPosition);
            RosterDay newDay = newList.get(newItemPosition);
            return Objects.equals(oldDay.getRosterItems(), newDay.getRosterItems());
        }
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
            if (textViewDate != null) {
                textViewDate.setText(rosterDay.getLocalDate().format(dateFormatter));
            }
            layoutRosterItems.removeAllViews();

            boolean isDarkMode = (itemView.getContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            List<RosterItem> items = rosterDay.getRosterItems();

            for (RosterItem item : items) {
                View subItemView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_roster_shift_branch, layoutRosterItems, false);

                TextView textViewEmployeeName = subItemView.findViewById(R.id.textViewEmployeeName);
                TextView textViewShiftTime = subItemView.findViewById(R.id.textViewShiftTime);
                TextView textViewPause = subItemView.findViewById(R.id.textViewPause);
                TextView textViewComment = subItemView.findViewById(R.id.textViewComment);

                // Data binding
                Employee employee = employeeMap.get(item.getEmployeeKey());
                String name = (employee != null) ? employee.getEmployeeFullName() : itemView.getContext().getString(R.string.unknown_with_id, item.getEmployeeKey());
                textViewEmployeeName.setText(name);

                String start = item.getDutyStartDateTime().format(timeFormatter);
                String end = item.getDutyEndDateTime().format(timeFormatter);
                textViewShiftTime.setText(itemView.getContext().getString(R.string.time_range_format, start, end));

                if (item.getBreakStartDateTime() != null && item.getBreakEndDateTime() != null) {
                    String pStart = item.getBreakStartDateTime().format(timeFormatter);
                    String pEnd = item.getBreakEndDateTime().format(timeFormatter);
                    String pauseRange = itemView.getContext().getString(R.string.time_range_format, pStart, pEnd);
                    textViewPause.setText(itemView.getContext().getString(R.string.pause_format, pauseRange));
                    textViewPause.setVisibility(View.VISIBLE);
                } else {
                    textViewPause.setVisibility(View.GONE);
                }

                if (item.getComment() != null && !item.getComment().isEmpty()) {
                    textViewComment.setText(item.getComment());
                    textViewComment.setVisibility(View.VISIBLE);
                } else {
                    textViewComment.setVisibility(View.GONE);
                }

                // Color Logic
                MaterialCardView cardView = (MaterialCardView) subItemView;
                int backgroundColor;

                if (employee != null) {
                    String profession = employee.getEmployeeProfession();
                    if ("Apotheker".equalsIgnoreCase(profession)) {
                        backgroundColor = itemView.getContext().getColor(R.color.md_theme_primaryContainer);
                    } else if ("Pharmazieingenieur".equalsIgnoreCase(profession) || "PI".equalsIgnoreCase(profession)) {
                        backgroundColor = itemView.getContext().getColor(R.color.md_theme_secondaryContainer);
                    } else if ("PTA".equalsIgnoreCase(profession)) {
                        backgroundColor = itemView.getContext().getColor(R.color.md_theme_tertiaryContainer);
                    } else {
                        backgroundColor = isDarkMode
                                ? itemView.getContext().getColor(R.color.md_theme_surfaceContainerHigh)
                                : itemView.getContext().getColor(R.color.md_theme_surfaceVariant);
                    }
                } else {
                    backgroundColor = isDarkMode
                            ? itemView.getContext().getColor(R.color.md_theme_surfaceContainerHigh)
                            : itemView.getContext().getColor(R.color.md_theme_surfaceVariant);
                }

                if (isDarkMode) {
                    backgroundColor = ColorUtils.adjustColorForDarkMode(backgroundColor);
                }

                int fontColor = ColorUtils.getContrastColor(backgroundColor);
                int secondaryFontColor = isDarkMode ? itemView.getContext().getColor(R.color.md_theme_onSurfaceVariant) : fontColor;

                cardView.setCardBackgroundColor(backgroundColor);
                cardView.setStrokeColor(itemView.getContext().getColor(R.color.md_theme_outlineVariant));

                textViewEmployeeName.setTextColor(fontColor);
                textViewShiftTime.setTextColor(isDarkMode && fontColor == android.graphics.Color.WHITE
                        ? itemView.getContext().getColor(R.color.md_theme_primary) : fontColor);
                textViewPause.setTextColor(secondaryFontColor);
                textViewComment.setTextColor(secondaryFontColor);

                layoutRosterItems.addView(subItemView);
            }
        }
    }
}
