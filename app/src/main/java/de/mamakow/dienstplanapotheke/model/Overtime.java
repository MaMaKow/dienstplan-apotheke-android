package de.mamakow.dienstplanapotheke.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.Objects;

@Entity(tableName = "overtime_table", primaryKeys = {"employee_key", "date"})
public class Overtime {

    @SerializedName(value = "id", alternate = {"primary_key", "pk"})
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName(value = "employeeKey", alternate = {"employee_key", "employee_id", "employeeId"})
    @ColumnInfo(name = "employee_key")
    private int employeeKey;

    @SerializedName(value = "hours", alternate = {"overtime_hours"})
    @ColumnInfo(name = "hours")
    private double hours;

    @SerializedName("balance")
    @ColumnInfo(name = "balance")
    private double balance;

    @NonNull
    @SerializedName(value = "date", alternate = {"overtime_date"})
    @ColumnInfo(name = "date")
    private LocalDate date;

    @SerializedName("reason")
    @ColumnInfo(name = "reason")
    private String reason;

    public Overtime() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeKey() {
        return employeeKey;
    }

    public void setEmployeeKey(int employeeKey) {
        this.employeeKey = employeeKey;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Overtime overtime = (Overtime) o;
        return employeeKey == overtime.employeeKey &&
                Objects.equals(date, overtime.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeKey, date);
    }
}
