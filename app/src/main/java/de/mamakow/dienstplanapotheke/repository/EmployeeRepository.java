package de.mamakow.dienstplanapotheke.repository;

import de.mamakow.dienstplanapotheke.database.EmployeeDao;
import de.mamakow.dienstplanapotheke.model.Employee;

public class EmployeeRepository {
    private final EmployeeDao employeeDao;

    public EmployeeRepository(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    public Employee getEmployeeByEmployeeKey(int employeeKey) {
        return employeeDao.getEmployeeByEmployeeKey(employeeKey);
    }

    public Employee getEmployeeById(int id) {
        return employeeDao.getEmployeeById(id);
    }
}