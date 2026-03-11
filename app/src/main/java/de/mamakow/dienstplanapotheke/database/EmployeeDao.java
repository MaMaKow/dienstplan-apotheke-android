package de.mamakow.dienstplanapotheke.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.mamakow.dienstplanapotheke.model.Employee;

@Dao
public interface EmployeeDao {
    @Query("SELECT * FROM employee_table WHERE employee_key = :employeeKey")
    Employee getEmployeeByEmployeeKey(int employeeKey);

    @Query("SELECT * FROM employee_table WHERE employee_key = :id")
    Employee getEmployeeById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEmployees(List<Employee> employees);

    @Query("DELETE FROM employee_table")
    void clearEmployees();

    @Query("SELECT * FROM employee_table")
    List<Employee> getAllEmployees();
}
