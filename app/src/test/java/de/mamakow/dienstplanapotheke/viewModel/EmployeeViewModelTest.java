package de.mamakow.dienstplanapotheke.viewModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.os.Build;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Workforce;
import de.mamakow.dienstplanapotheke.repository.AbsenceRepository;
import de.mamakow.dienstplanapotheke.repository.BranchRepository;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;
import de.mamakow.dienstplanapotheke.repository.OvertimeRepository;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.TIRAMISU})
public class EmployeeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private MainViewModel viewModel;
    private AutoCloseable mocksCloseable;

    @Mock
    private RosterRepository rosterRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private AbsenceRepository absenceRepository;
    @Mock
    private OvertimeRepository overtimeRepository;
    @Mock
    private SessionManager sessionManager;

    @Before
    public void setUp() throws Exception {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        Application application = RuntimeEnvironment.getApplication();

        when(sessionManager.getUserEmployeeKey()).thenReturn(-1);

        // Use the DI constructor to avoid Room initialization
        viewModel = new MainViewModel(
                application,
                rosterRepository,
                employeeRepository,
                branchRepository,
                absenceRepository,
                overtimeRepository,
                sessionManager
        );
    }

    @After
    public void tearDown() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void testSetSelectedEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeKey(123);

        viewModel.setSelectedEmployee(employee);

        assertEquals(employee, viewModel.getSelectedEmployee().getValue());
    }

    @Test
    public void testGetWorkforce() {
        MutableLiveData<Workforce> workforceData = new MutableLiveData<>();
        when(employeeRepository.getWorkforceLiveData()).thenReturn(workforceData);

        viewModel.getWorkforce();

        verify(employeeRepository).getWorkforceLiveData();
    }
}
