package de.mamakow.dienstplanapotheke.model.dto;

import com.google.gson.annotations.SerializedName;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mamakow.dienstplanapotheke.model.RosterItem;

public class RosterItemDto {

    @SerializedName("date_sql")
    public String dateSql;
    @SerializedName("employee_key")
    public Integer employeeKey;
    @SerializedName("branch_id")
    public int branchId;
    @SerializedName("duty_start_sql")
    public String dutyStartSql;
    @SerializedName("duty_end_sql")
    public String dutyEndSql;
    @SerializedName("break_start_sql")
    public String breakStartSql;
    @SerializedName("break_end_sql")
    public String breakEndSql;
    public String comment;

    // Standard-Konstruktor für GSON
    public RosterItemDto() {
    }

    public static Map<String, List<RosterItemDto>> mapToApiFormat(List<RosterItem> items) {
        Map<String, List<RosterItemDto>> map = new HashMap<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (RosterItem item : items) {
            RosterItemDto dto = new RosterItemDto();
            dto.dateSql = item.getLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            dto.employeeKey = item.getEmployeeKey();
            dto.branchId = item.getBranchId();

            // Format times as HH:mm as expected by the PHP backend
            dto.dutyStartSql = item.getDutyStartDateTime() != null ? item.getDutyStartDateTime().format(timeFormatter) : null;
            dto.dutyEndSql = item.getDutyEndDateTime() != null ? item.getDutyEndDateTime().format(timeFormatter) : null;
            dto.breakStartSql = item.getBreakStartDateTime() != null ? item.getBreakStartDateTime().format(timeFormatter) : null;
            dto.breakEndSql = item.getBreakEndDateTime() != null ? item.getBreakEndDateTime().format(timeFormatter) : null;
            dto.comment = item.getComment();

            // PHP expects the Unix timestamp as a String key for the day
            long unixTimestamp = item.getLocalDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String key = String.valueOf(unixTimestamp);

            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(dto);
        }
        return map;
    }

}
