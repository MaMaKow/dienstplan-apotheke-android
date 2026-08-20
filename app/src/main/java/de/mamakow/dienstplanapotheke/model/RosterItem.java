package de.mamakow.dienstplanapotheke.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(tableName = "roster_table", primaryKeys = {"employee_key", "local_date", "duty_start_date_time"})
public class RosterItem {

    @NonNull
    @SerializedName(value = "date", alternate = {"localDate", "local_date"})
    @ColumnInfo(name = "local_date")
    private LocalDate localDate;

    @NonNull
    @SerializedName(value = "employeeKey", alternate = {"employee_key", "employee_id", "employeeId"})
    @ColumnInfo(name = "employee_key")
    private int employeeKey;

    @SerializedName(value = "branch_id", alternate = {"branchId", "branch"})
    @ColumnInfo(name = "branch_id")
    private int branchId;

    @SerializedName("comment")
    @ColumnInfo(name = "comment")
    private String comment;

    @NonNull
    @SerializedName(value = "duty_start", alternate = {"dutyStart", "duty_start_time"})
    @ColumnInfo(name = "duty_start_date_time")
    private LocalDateTime dutyStartDateTime;

    @SerializedName(value = "duty_end", alternate = {"dutyEnd", "duty_end_time"})
    @ColumnInfo(name = "duty_end_date_time")
    private LocalDateTime dutyEndDateTime;

    @SerializedName(value = "break_start", alternate = {"breakStart"})
    @ColumnInfo(name = "break_start_date_time")
    private LocalDateTime breakStartDateTime;

    @SerializedName(value = "break_end", alternate = {"breakEnd"})
    @ColumnInfo(name = "break_end_date_time")
    private LocalDateTime breakEndDateTime;

    @SerializedName(value = "working_hours", alternate = {"workingHours"})
    @ColumnInfo(name = "working_hours")
    private float workingHours;

    @ColumnInfo(name = "dt_stamp")
    private LocalDateTime dtStamp;

    @ColumnInfo(name = "status")
    private Status status;

    public RosterItem() {
    }

    /**
     * Berechnet die Netto-Arbeitszeit in Stunden (Dienstzeit minus Pausenzeit).
     */
    public double calculateNetWorkingHours() {
        if (dutyStartDateTime == null || dutyEndDateTime == null) {
            return 0;
        }

        Duration totalDuty = Duration.between(dutyStartDateTime, dutyEndDateTime);
        long totalMinutes = totalDuty.toMinutes();

        if (breakStartDateTime != null && breakEndDateTime != null) {
            Duration breakDuration = Duration.between(breakStartDateTime, breakEndDateTime);
            totalMinutes -= breakDuration.toMinutes();
        }

        return Math.max(0, totalMinutes / 60.0);
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public LocalDateTime getDutyEndDateTime() {
        return dutyEndDateTime;
    }

    public void setDutyEndDateTime(LocalDateTime dutyEndDateTime) {
        this.dutyEndDateTime = dutyEndDateTime;
    }

    public LocalDateTime getBreakStartDateTime() {
        return breakStartDateTime;
    }

    public void setBreakStartDateTime(LocalDateTime breakStartDateTime) {
        this.breakStartDateTime = breakStartDateTime;
    }

    public LocalDateTime getBreakEndDateTime() {
        return breakEndDateTime;
    }

    public void setBreakEndDateTime(LocalDateTime breakEndDateTime) {
        this.breakEndDateTime = breakEndDateTime;
    }

    @NonNull
    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(@NonNull LocalDate localDate) {
        this.localDate = localDate;
    }

    public int getEmployeeKey() {
        return employeeKey;
    }

    public void setEmployeeKey(int employeeKey) {
        this.employeeKey = employeeKey;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @NonNull
    public LocalDateTime getDutyStartDateTime() {
        return dutyStartDateTime;
    }

    public void setDutyStartDateTime(@NonNull LocalDateTime dutyStartDateTime) {
        this.dutyStartDateTime = dutyStartDateTime;
    }

    public float getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(float workingHours) {
        this.workingHours = workingHours;
    }

    public LocalDateTime getDtStamp() {
        return dtStamp;
    }

    public void setDtStamp(LocalDateTime dtStamp) {
        this.dtStamp = dtStamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RosterItem that = (RosterItem) o;
        return employeeKey == that.employeeKey &&
                branchId == that.branchId &&
                Float.compare(that.workingHours, workingHours) == 0 &&
                Objects.equals(localDate, that.localDate) &&
                Objects.equals(dutyStartDateTime, that.dutyStartDateTime) &&
                Objects.equals(dutyEndDateTime, that.dutyEndDateTime) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDate, employeeKey, dutyStartDateTime);
    }

    public enum Status {
        TENTATIVE, CONFIRMED, CANCELLED
    }
}
