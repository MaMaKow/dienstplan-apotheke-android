package de.mamakow.dienstplanapotheke.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

@Entity(tableName = "overtime_table")
public class Overtime {

    @PrimaryKey(autoGenerate = true)
    private int dbId;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("employeeKey")
    @ColumnInfo(name = "employee_key")
    private int employeeKey;
    @SerializedName("hours")
    @ColumnInfo(name = "hours")
    private double hours;
    @SerializedName("balance")
    @ColumnInfo(name = "balance")
    private double balance;

    @SerializedName("date")
    @ColumnInfo(name = "date")
    private LocalDate date;


    @SerializedName("reason")
    @ColumnInfo(name = "reason")
    private String reason;

    public Overtime() {
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
