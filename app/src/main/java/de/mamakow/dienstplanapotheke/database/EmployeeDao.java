package de.mamakow.dienstplanapotheke.database;

import androidx.room.Dao;
import androidx.room.Query;

import de.mamakow.dienstplanapotheke.model.Employee;

@Dao
public interface EmployeeDao {
    @Query("SELECT * FROM employee_table WHERE employee_key = :employeeKey")
    Employee getEmployeeByEmployeeKey(int employeeKey);

    @Query("SELECT * FROM employee_table WHERE id = :id")
    Employee getEmployeeById(int id);
}
