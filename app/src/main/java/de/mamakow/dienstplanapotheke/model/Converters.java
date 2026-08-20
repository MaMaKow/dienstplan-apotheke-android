package de.mamakow.dienstplanapotheke.model;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Converters {
    private static final Gson gson = new Gson();
    private final DateTimeFormatter localDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DateTimeFormatter localTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @TypeConverter
    public static String fromOpeningTimesMap(HashMap<Integer, OpeningHours> value) {
        return gson.toJson(value);
    }

    @TypeConverter
    public static HashMap<Integer, OpeningHours> toOpeningTimesMap(String value) {
        Type mapType = new TypeToken<HashMap<Integer, OpeningHours>>() {
        }.getType();
        return gson.fromJson(value, mapType);
    }

    @TypeConverter
    public String fromLocalDate(LocalDate value) {
        if (value == null) return null;
        return value.format(localDateFormatter);
    }

    @TypeConverter
    public String fromLocalDateTime(LocalDateTime value) {
        if (value == null) return null;
        return value.format(localDateTimeFormatter);
    }

    @TypeConverter
    public String fromLocalTime(LocalTime value) {
        if (value == null) return null;
        return value.format(localTimeFormatter);
    }

    @TypeConverter
    public LocalDate toLocalDate(String value) {
        if (value == null) return null;
        return LocalDate.parse(value, localDateFormatter);
    }

    @TypeConverter
    public LocalDateTime toLocalDateTime(String value) {
        if (value == null) return null;
        return LocalDateTime.parse(value, localDateTimeFormatter);
    }

    @TypeConverter
    public LocalTime toLocalTime(String value) {
        if (value == null) return null;
        return LocalTime.parse(value, localTimeFormatter);
    }

    @TypeConverter
    public String fromStatus(RosterItem.Status value) {
        if (value == null) return null;
        return value.name();
    }

    @TypeConverter
    public RosterItem.Status toStatus(String value) {
        if (value == null) return null;
        return RosterItem.Status.valueOf(value);
    }
}
