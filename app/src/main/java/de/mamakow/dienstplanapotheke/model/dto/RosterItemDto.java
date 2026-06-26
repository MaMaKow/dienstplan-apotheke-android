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

        for (RosterItem item : items) {
            RosterItemDto dto = new RosterItemDto();
            dto.dateSql = item.getLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            dto.employeeKey = item.getEmployeeKey();
            dto.branchId = item.getBranchId();
            // Hier die Zeiten formatieren (HH:mm), wie PHP es erwartet
            dto.dutyStartSql = item.getDutyStartDateTime() != null ? item.getDutyStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
            dto.dutyEndSql = item.getDutyEndDateTime() != null ? item.getDutyEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
            dto.breakStartSql = item.getBreakStartDateTime() != null ? item.getBreakStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
            dto.breakEndSql = item.getBreakEndDateTime() != null ? item.getBreakEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
            dto.comment = item.getComment();

            // PHP erwartet den Unix-Timestamp als String-Key für den Tag
            // Beispiel für LocalDate zu Unix (Mitternacht UTC/Lokal)
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
