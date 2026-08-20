package de.mamakow.dienstplanapotheke.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.Objects;

@Entity(tableName = "absence_table", primaryKeys = {"employee_key", "start_date"})
public class Absence {

    @SerializedName(value = "id", alternate = {"primary_key", "pk"})
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName(value = "employeeKey", alternate = {"employee_key", "employee_id", "employeeId"})
    @ColumnInfo(name = "employee_key")
    private int employeeKey;

    @NonNull
    @SerializedName(value = "start", alternate = {"start_date", "startDate"})
    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @SerializedName(value = "end", alternate = {"end_date", "endDate"})
    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    @SerializedName(value = "reasonId", alternate = {"reason_id", "absence_type", "type"})
    @ColumnInfo(name = "absence_type")
    private int absenceType;

    @SerializedName(value = "reasonString", alternate = {"reason_string", "absence_type_string"})
    @ColumnInfo(name = "absence_type_string")
    private String absenceTypeString;

    @SerializedName("comment")
    @ColumnInfo(name = "comment")
    private String comment;

    public Absence() {
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

    @NonNull
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NonNull LocalDate startDate) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Absence absence = (Absence) o;
        return employeeKey == absence.employeeKey &&
                Objects.equals(startDate, absence.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeKey, startDate);
    }
}
