package de.mamakow.dienstplanapotheke.view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.AbsenceDayData;
import de.mamakow.dienstplanapotheke.model.AbsenceMonthData;

public class HeatmapAdapter extends ListAdapter<AbsenceMonthData, HeatmapAdapter.MonthViewHolder> {

    private final OnDayClickListener listener;

    public HeatmapAdapter(OnDayClickListener listener) {
        super(new DiffUtil.ItemCallback<AbsenceMonthData>() {
            @Override
            public boolean areItemsTheSame(@NonNull AbsenceMonthData oldItem, @NonNull AbsenceMonthData newItem) {
                return oldItem.getYearMonth().equals(newItem.getYearMonth());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AbsenceMonthData oldItem, @NonNull AbsenceMonthData newItem) {
                return oldItem.getDayDataMap().equals(newItem.getDayDataMap());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MonthViewHolder(parent.getContext(), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public interface OnDayClickListener {
        void onDayClick(LocalDate date, AbsenceDayData data);

        void onDayLongClick(LocalDate date, AbsenceDayData data, View view);
    }

    public static class MonthViewHolder extends RecyclerView.ViewHolder {
        private final Context context;
        private final OnDayClickListener listener;
        private final LinearLayout container;
        private final TextView titleView;
        private final GridLayout daysGrid;
        private final String[] weekDays = {"M", "D", "M", "D", "F", "S", "S"};

        public MonthViewHolder(@NonNull Context context, OnDayClickListener listener) {
            super(new LinearLayout(context));
            this.context = context;
            this.listener = listener;
            this.container = (LinearLayout) itemView;
            this.container.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, (int) (16 * context.getResources().getDisplayMetrics().density));
            this.container.setLayoutParams(params);

            // Title
            titleView = new TextView(context);
            titleView.setTextSize(16);
            titleView.setTypeface(null, Typeface.BOLD);
            titleView.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * context.getResources().getDisplayMetrics().density));
            titleParams.setMargins((int) (16 * context.getResources().getDisplayMetrics().density), 0, 0, 0);
            titleView.setLayoutParams(titleParams);
            container.addView(titleView);

            // Weekday Header
            LinearLayout headerRow = new LinearLayout(context);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) (32 * context.getResources().getDisplayMetrics().density)));

            for (String day : weekDays) {
                TextView tv = new TextView(context);
                tv.setText(day);
                tv.setTextSize(12);
                tv.setTextColor(ContextCompat.getColor(context, R.color.heatmap_text_gray));
                tv.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                tv.setLayoutParams(lp);
                headerRow.addView(tv);
            }
            container.addView(headerRow);

            // Grid for days
            daysGrid = new GridLayout(context);
            daysGrid.setColumnCount(7);
            daysGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
            LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            gridParams.gravity = Gravity.CENTER_HORIZONTAL;
            daysGrid.setLayoutParams(gridParams);
            container.addView(daysGrid);
        }

        public void bind(AbsenceMonthData monthData) {
            String monthName = monthData.getYearMonth().getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN);
            titleView.setText(monthName + " " + monthData.getYearMonth().getYear());

            daysGrid.removeAllViews();

            int cellMargin = (int) (2 * context.getResources().getDisplayMetrics().density);
            int cellSize = (int) (44 * context.getResources().getDisplayMetrics().density);

            // Add empty cells for the start of the month
            LocalDate firstDay = monthData.getYearMonth().atDay(1);
            int dayOfWeek = firstDay.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)
            for (int i = 1; i < dayOfWeek; i++) {
                View emptyView = new View(context);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(cellMargin, cellMargin, cellMargin, cellMargin);
                emptyView.setLayoutParams(params);
                daysGrid.addView(emptyView);
            }

            // Add actual days
            for (LocalDate date : monthData.getDays()) {
                TextView dayView = new TextView(context);
                dayView.setText(String.valueOf(date.getDayOfMonth()));
                dayView.setGravity(Gravity.CENTER);
                dayView.setTextSize(14);

                AbsenceDayData data = monthData.getDayDataMap().get(date.toEpochDay());
                int colorRes = R.color.heatmap_0;
                if (data != null) {
                    int count = data.getCount();
                    int pharmacists = data.getPharmacistCount();

                    if (pharmacists >= 3 || count >= 4) {
                        colorRes = R.color.heatmap_4_plus;
                    } else if (pharmacists >= 2 || count >= 3) {
                        colorRes = R.color.heatmap_3;
                    } else if (count == 2) {
                        colorRes = R.color.heatmap_2;
                    } else if (count == 1) {
                        colorRes = R.color.heatmap_1;
                    }
                }

                dayView.setBackgroundColor(ContextCompat.getColor(context, colorRes));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(cellMargin, cellMargin, cellMargin, cellMargin);
                dayView.setLayoutParams(params);

                dayView.setOnClickListener(v -> {
                    if (listener != null) listener.onDayClick(date, data);
                });

                dayView.setOnLongClickListener(v -> {
                    if (listener != null) listener.onDayLongClick(date, data, v);
                    return true;
                });

                daysGrid.addView(dayView);
            }
        }
    }
}
