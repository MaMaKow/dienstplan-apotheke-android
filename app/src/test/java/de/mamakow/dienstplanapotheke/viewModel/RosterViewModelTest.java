package de.mamakow.dienstplanapotheke.viewModel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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

import java.time.LocalDate;

import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.repository.AbsenceRepository;
import de.mamakow.dienstplanapotheke.repository.BranchRepository;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;
import de.mamakow.dienstplanapotheke.repository.OvertimeRepository;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.TIRAMISU})
public class RosterViewModelTest {

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

        // Ensure selectedEmployee is not null so switchMap triggers
        when(sessionManager.getUserEmployeeKey()).thenReturn(1);
        when(sessionManager.getUserDisplayName()).thenReturn("Test User");

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
    public void testSetSelectedDate() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        viewModel.setSelectedDate(date);
        assertEquals(date, viewModel.getSelectedDate().getValue());
    }

    @Test
    public void testGetEmployeeRoster() {
        MutableLiveData<Roster> rosterData = new MutableLiveData<>();
        when(rosterRepository.getRosterData(any(), any(), any(), any())).thenReturn(rosterData);

        // Observe the LiveData to trigger the switchMap
        viewModel.getEmployeeRoster().observeForever(roster -> {
        });

        // Setting values to trigger the MediatorLiveData -> switchMap
        Employee employee = new Employee();
        employee.setEmployeeKey(1);
        viewModel.setSelectedEmployee(employee);

        verify(rosterRepository, atLeastOnce()).getRosterData(any(), any(), any(), any());
    }
}
