/*
 * Copyright (C) 2021 Mandelkow
 *
 * Dienstplan Apotheke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.mamakow.dienstplanapotheke.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;

public class Employee {

    private final int employeeKey;
    private final String employeeLastName;
    private final String employeeFirstName;
    private final String employeeProfession;
    private final float employeeWorkingHours;
    private final int employeeLunchBreakMinutes;
    private final int employeeHolidays;
    private final boolean employeeAbilitiesGoodsReceipt;
    private final boolean employeeAbilitiesCompounding;
    private int employeeBranchId;
    private LocalDate employeeStartOfEmployment;
    private LocalDate employeeEndOfEmployment;

    @RequiresApi(api = Build.VERSION_CODES.O)
    // This was inserted to allow the use of LocalDate.parse()
    public Employee(String employeeKey,
                    String employeeLastName,
                    String employeeFirstName,
                    String employeeProfession,
                    String employeeWorkingHours,
                    String employeeLunchBreakMinutes,
                    String employeeHolidays,
                    String employeeBranchName,
                    String employeeAbilitiesGoodsReceipt,
                    String employeeAbilitiesCompounding,
                    String employeeStartOfEmployment,
                    String employeeEndOfEmployment
    ) {
        this.employeeKey = Integer.parseInt(employeeKey);
        this.employeeLastName = employeeLastName;
        this.employeeFirstName = employeeFirstName;
        this.employeeProfession = employeeProfession;
        this.employeeWorkingHours = Float.parseFloat(employeeWorkingHours);
        this.employeeLunchBreakMinutes = Integer.parseInt(employeeLunchBreakMinutes);
        this.employeeHolidays = Integer.parseInt(employeeHolidays);
        NetworkOfBranchOffices networkOfBranchOffices = new NetworkOfBranchOffices();
        Branch branch = networkOfBranchOffices.getBranchByName(employeeBranchName);
        this.employeeBranchId = branch.getBranchId();
        this.employeeAbilitiesGoodsReceipt = Boolean.parseBoolean(employeeAbilitiesGoodsReceipt);
        this.employeeAbilitiesCompounding = Boolean.parseBoolean(employeeAbilitiesCompounding);
        /**
         * Employment:
         */
        this.employeeStartOfEmployment = null;
        this.employeeEndOfEmployment = null;
        this.employeeStartOfEmployment = LocalDate.parse(employeeStartOfEmployment, Wrapper.DATE_TIME_FORMATTER_DAY_MONTH_YEAR);
        this.employeeEndOfEmployment = LocalDate.parse(employeeEndOfEmployment, Wrapper.DATE_TIME_FORMATTER_DAY_MONTH_YEAR);
    }

    public int getEmployeeKey() {
        return employeeKey;
    }

    public String getLastName() {
        return employeeLastName;
    }

    public String getFirstName() {
        return employeeFirstName;
    }

    public String getFullName() {
        return employeeFirstName + " " + employeeLastName;
    }

    public String getProfession() {
        return employeeProfession;
    }

    public float getWorkingHours() {
        return employeeWorkingHours;
    }

    public int getHolidays() {
        return employeeHolidays;
    }

}
