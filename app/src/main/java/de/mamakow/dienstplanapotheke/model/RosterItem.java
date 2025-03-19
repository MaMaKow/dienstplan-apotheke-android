package de.mamakow.dienstplanapotheke.model;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.mamakow.dienstplanapotheke.R;

@Entity(tableName = "roster_table")
public class RosterItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    /**
     * The primary key in the remote database.
     * This is not the same key as in the local Room database.
     */
    @ColumnInfo(name = "remote_primary_key")
    private int remotePrimaryKey;
    @ColumnInfo(name = "local_date")
    private LocalDate localDate;
    @ColumnInfo(name = "employee_key")
    private int employeeKey;
    @ColumnInfo(name = "branch_id")
    private int branchId;
    @ColumnInfo(name = "comment")
    private String comment;
    @ColumnInfo(name = "duty_start_date_time")
    private LocalDateTime dutyStartDateTime;
    @ColumnInfo(name = "duty_end_date_time")
    private LocalDateTime dutyEndDateTime;
    @ColumnInfo(name = "break_start_date_time")
    private LocalDateTime breakStartDateTime;
    @ColumnInfo(name = "break_end_date_time")
    private LocalDateTime breakEndDateTime;
    @ColumnInfo(name = "working_hours")
    private float workingHours;
    /**
     * The date and time the calendar information was last updated.
     */
    @ColumnInfo(name = "dt_stamp")
    private LocalDateTime dtStamp;
    @ColumnInfo(name = "status")
    private Status status;

    /**
     * public RosterItem(int remotePrimaryKey, LocalDate localDate, int employeeKey, int branchId, String comment, LocalDateTime dutyStartDateTime, LocalDateTime dutyEndDateTime, LocalDateTime breakStartDateTime, LocalDateTime breakEndDateTime, float workingHours, LocalDateTime dtStamp, Status status) {
     * this.remotePrimaryKey = remotePrimaryKey;
     * this.localDate = localDate;
     * this.employeeKey = employeeKey;
     * this.branchId = branchId;
     * this.comment = comment;
     * this.dutyStartDateTime = dutyStartDateTime;
     * this.dutyEndDateTime = dutyEndDateTime;
     * this.breakStartDateTime = breakStartDateTime;
     * this.breakEndDateTime = breakEndDateTime;
     * this.workingHours = workingHours;
     * this.dtStamp = dtStamp;
     * this.status = status;
     * }
     */
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

    public String getICalendarLocation(@NotNull Context context) {
        Branch branch = new NetworkOfBranchOffices().getBranchById(branchId);
        if (branch == null) {
            return context.getString(R.string.unknown_branch);
        }
        return branch.getBranchName() + System.lineSeparator() + branch.getBranchAddress();
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

    /**
     * @return String Name keines Mitarbeiters
     * @// TODO: 14.03.25 Diese Funktion muss durch getFullName() ersetztt werden.
     * @deprecated Mockupfunktion ohne Zugriff auf echte Daten
     */
    public String getFullNamePseudo() {
        return "John Doe";
    }

    public String getFullName() throws Exception {
        throw new Exception("Not implemented yet");
        //return new EmployeeRepository().getEmployeeById(employeeKey).getFullName();
    }

    public enum Status {
        TENTATIVE, CONFIRMED, CANCELLED;
    }
}