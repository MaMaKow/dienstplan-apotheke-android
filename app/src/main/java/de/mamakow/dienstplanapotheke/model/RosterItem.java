package de.mamakow.dienstplanapotheke.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(tableName = "roster_table")
public class RosterItem {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "remote_primary_key")
    private int remotePrimaryKey;

    @SerializedName("date")
    @ColumnInfo(name = "local_date")
    private LocalDate localDate;

    @SerializedName("employee_key")
    @ColumnInfo(name = "employee_key")
    private int employeeKey;

    @SerializedName("branch_id")
    @ColumnInfo(name = "branch_id")
    private int branchId;

    @SerializedName("comment")
    @ColumnInfo(name = "comment")
    private String comment;

    @SerializedName("duty_start")
    @ColumnInfo(name = "duty_start_date_time")
    private LocalDateTime dutyStartDateTime;

    @SerializedName("duty_end")
    @ColumnInfo(name = "duty_end_date_time")
    private LocalDateTime dutyEndDateTime;

    @SerializedName("break_start")
    @ColumnInfo(name = "break_start_date_time")
    private LocalDateTime breakStartDateTime;

    @SerializedName("break_end")
    @ColumnInfo(name = "break_end_date_time")
    private LocalDateTime breakEndDateTime;

    @SerializedName("working_hours")
    @ColumnInfo(name = "working_hours")
    private float workingHours;

    @ColumnInfo(name = "dt_stamp")
    private LocalDateTime dtStamp;

    @ColumnInfo(name = "status")
    private Status status;

    public RosterItem() {
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

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
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

    public LocalDateTime getDutyStartDateTime() {
        return dutyStartDateTime;
    }

    public void setDutyStartDateTime(LocalDateTime dutyStartDateTime) {
        this.dutyStartDateTime = dutyStartDateTime;
    }

    public int getRemotePrimaryKey() {
        return remotePrimaryKey;
    }

    public void setRemotePrimaryKey(int remotePrimaryKey) {
        this.remotePrimaryKey = remotePrimaryKey;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public enum Status {
        TENTATIVE, CONFIRMED, CANCELLED
    }
}
