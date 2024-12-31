package de.mamakow.dienstplanapotheke.model;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class DateTime {
    public static final DateTimeFormatter DATE_TIME_FORMATTER_DAY_MONTH_YEAR;
    public static final DateTimeFormatter DATE_TIME_FORMATTER_YEAR_MONTH_DAY;

    static {
        DATE_TIME_FORMATTER_DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY);
        DATE_TIME_FORMATTER_YEAR_MONTH_DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.GERMANY);
    }
}
