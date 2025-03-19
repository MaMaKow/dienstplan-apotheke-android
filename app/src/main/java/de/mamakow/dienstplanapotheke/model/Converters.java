package de.mamakow.dienstplanapotheke.model;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Converters {
    private final DateTimeFormatter localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @TypeConverter
    public String fromLocalDate(LocalDate value) {
        if (value == null) {
            return null;
        }
        return value.format(localDateFormatter);
    }

    @TypeConverter
    public String fromLocalDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(localDateTimeFormatter);
    }

    @TypeConverter
    public LocalDate toLocalDate(String value) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value, localDateFormatter);
    }

    @TypeConverter
    public LocalDateTime toLocalDateTime(String value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(value, localDateTimeFormatter);
    }

    @TypeConverter
    public String fromStatus(RosterItem.Status value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @TypeConverter
    public RosterItem.Status toStatus(String value) {
        if (value == null) {
            return null;
        }
        return RosterItem.Status.valueOf(value);
    }
}
