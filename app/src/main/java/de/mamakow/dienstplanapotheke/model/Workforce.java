package de.mamakow.dienstplanapotheke.model;

import java.util.ArrayList;
import java.util.List;

public class Workforce {
    private final List<Employee> listOfEmployees;

    public Workforce(List<Employee> employees) {
        this.listOfEmployees = employees != null ? employees : new ArrayList<>();
    }

    public Employee findByKey(int key) {
        if (key == -1) return null;
        for (Employee e : listOfEmployees) {
            if (e.getEmployeeKey() == key) {
                return e;
            }
        }
        return null;
    }

    public List<String> getEmployeeNames() {
        List<String> employeeNames = new ArrayList<>();
        for (int i = 0; i < listOfEmployees.size(); i++) {
            Employee employee = listOfEmployees.get(i);
            employeeNames.add(employee.getEmployeeFullName());
        }
        return employeeNames;
    }

    public List<Employee> getEmployees() {
        return listOfEmployees;
    }

    public boolean isEmpty() {
        return listOfEmployees.isEmpty();
    }

    public int getCount() {
        return listOfEmployees.size();
    }
}
