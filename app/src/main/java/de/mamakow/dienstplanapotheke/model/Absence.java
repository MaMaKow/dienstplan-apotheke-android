package de.mamakow.dienstplanapotheke.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;

@Entity(tableName = "absence_table")
public class Absence {

    @PrimaryKey(autoGenerate = true)
    private int dbId;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("employeeKey")
    @ColumnInfo(name = "employee_key")
    private int employeeKey;

    @SerializedName("start")
    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @SerializedName("end")
    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    @SerializedName("reasonId")
    @ColumnInfo(name = "absence_type")
    private int absenceType;
    @SerializedName("reasonString")
    @ColumnInfo(name = "absence_type_string")
    private String absenceTypeString;

    @SerializedName("comment")
    @ColumnInfo(name = "comment")
    private String comment;

    public Absence() {
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getAbsenceType() {
        return absenceType;
    }

    public void setAbsenceType(int absenceType) {
        this.absenceType = absenceType;
    }

    public String getAbsenceTypeString() {
        return absenceTypeString;
    }

    public void setAbsenceTypeString(String absenceTypeString) {
        this.absenceTypeString = absenceTypeString;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
