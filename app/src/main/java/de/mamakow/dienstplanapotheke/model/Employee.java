package de.mamakow.dienstplanapotheke.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "employee_table")
public class Employee {
    @PrimaryKey(autoGenerate = true)
    public int id; // Changed to public

    @ColumnInfo(name = "employee_key")
    private int employeeKey;
    @ColumnInfo(name = "employee_last_name")
    private String employeeLastName;
    @ColumnInfo(name = "employee_first_name")
    private String employeeFirstName;
    @ColumnInfo(name = "employee_profession")
    private String employeeProfession;
    @ColumnInfo(name = "employee_working_hours")
    private float employeeWorkingHours;
    @ColumnInfo(name = "employee_lunch_break_minutes")
    private int employeeLunchBreakMinutes;
    @ColumnInfo(name = "employee_holidays")
    private int employeeHolidays;
    @ColumnInfo(name = "employee_abilities_goods_receipt")
    private boolean employeeAbilitiesGoodsReceipt;
    @ColumnInfo(name = "employee_abilities_compounding")
    private boolean employeeAbilitiesCompounding;
    @ColumnInfo(name = "employee_branch_id")
    private int employeeBranchId;
    @ColumnInfo(name = "employee_start_of_employment")
    private LocalDate employeeStartOfEmployment;
    @ColumnInfo(name = "employee_end_of_employment")
    private LocalDate employeeEndOfEmployment;

    public Employee() {
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

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeFullName() {
        return employeeFirstName + " " + employeeLastName;
    }

    public String getEmployeeProfession() {
        return employeeProfession;
    }

    public void setEmployeeProfession(String employeeProfession) {
        this.employeeProfession = employeeProfession;
    }

    public float getEmployeeWorkingHours() {
        return employeeWorkingHours;
    }

    public void setEmployeeWorkingHours(float employeeWorkingHours) {
        this.employeeWorkingHours = employeeWorkingHours;
    }

    public int getEmployeeHolidays() {
        return employeeHolidays;
    }

    public void setEmployeeHolidays(int employeeHolidays) {
        this.employeeHolidays = employeeHolidays;
    }

    public boolean isEmployeeAbilitiesGoodsReceipt() {
        return employeeAbilitiesGoodsReceipt;
    }

    public void setEmployeeAbilitiesGoodsReceipt(boolean employeeAbilitiesGoodsReceipt) {
        this.employeeAbilitiesGoodsReceipt = employeeAbilitiesGoodsReceipt;
    }

    public boolean isEmployeeAbilitiesCompounding() {
        return employeeAbilitiesCompounding;
    }

    public void setEmployeeAbilitiesCompounding(boolean employeeAbilitiesCompounding) {
        this.employeeAbilitiesCompounding = employeeAbilitiesCompounding;
    }

    public int getEmployeeBranchId() {
        return employeeBranchId;
    }

    public void setEmployeeBranchId(int employeeBranchId) {
        this.employeeBranchId = employeeBranchId;
    }

    public LocalDate getEmployeeStartOfEmployment() {
        return employeeStartOfEmployment;
    }

    public void setEmployeeStartOfEmployment(LocalDate employeeStartOfEmployment) {
        this.employeeStartOfEmployment = employeeStartOfEmployment;
    }

    public LocalDate getEmployeeEndOfEmployment() {
        return employeeEndOfEmployment;
    }

    public void setEmployeeEndOfEmployment(LocalDate employeeEndOfEmployment) {
        this.employeeEndOfEmployment = employeeEndOfEmployment;
    }

    public int getEmployeeLunchBreakMinutes() {
        return employeeLunchBreakMinutes;
    }

    public void setEmployeeLunchBreakMinutes(int employeeLunchBreakMinutes) {
        this.employeeLunchBreakMinutes = employeeLunchBreakMinutes;
    }
}