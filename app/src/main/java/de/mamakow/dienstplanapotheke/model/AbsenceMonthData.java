package de.mamakow.dienstplanapotheke.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class AbsenceMonthData {
    private final YearMonth yearMonth;
    private final List<LocalDate> days;
    private final Map<Long, AbsenceDayData> dayDataMap;

    public AbsenceMonthData(YearMonth yearMonth, List<LocalDate> days, Map<Long, AbsenceDayData> dayDataMap) {
        this.yearMonth = yearMonth;
        this.days = days;
        this.dayDataMap = dayDataMap;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public List<LocalDate> getDays() {
        return days;
    }

    public Map<Long, AbsenceDayData> getDayDataMap() {
        return dayDataMap;
    }
}
