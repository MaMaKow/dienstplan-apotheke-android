package de.mamakow.dienstplanapotheke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.database.EmployeeDao;
import de.mamakow.dienstplanapotheke.model.Employee;

@RunWith(AndroidJUnit4.class)
public class EmployeeDatabaseTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private EmployeeDao employeeDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        employeeDao = db.employeeDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeKey(1);
        employee.setEmployeeFirstName("Max");
        employee.setEmployeeLastName("Mustermann");

        List<Employee> employees = new ArrayList<>();
        employees.add(employee);
        employeeDao.insertEmployees(employees);

        Employee retrieved = employeeDao.getEmployeeByEmployeeKey(1);
        assertNotNull(retrieved);
        assertEquals("Max", retrieved.getEmployeeFirstName());
    }

    @Test
    public void getAllEmployees() throws Exception {
        Employee e1 = new Employee();
        e1.setEmployeeKey(1);
        Employee e2 = new Employee();
        e2.setEmployeeKey(2);

        List<Employee> list = new ArrayList<>();
        list.add(e1);
        list.add(e2);
        employeeDao.insertEmployees(list);

        List<Employee> all = LiveDataTestUtil.getOrAwaitValue(employeeDao.getAllEmployeesLiveData());
        assertEquals(2, all.size());
    }

    @Test
    public void clearEmployees() {
        Employee e1 = new Employee();
        e1.setEmployeeKey(1);
        List<Employee> list = new ArrayList<>();
        list.add(e1);
        employeeDao.insertEmployees(list);

        employeeDao.clearEmployees();
        List<Employee> all = employeeDao.getAllEmployees();
        assertTrue(all.isEmpty());
    }
}
