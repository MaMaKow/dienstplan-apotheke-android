package de.mamakow.dienstplanapotheke.viewModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import android.os.Build;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.time.LocalDate;

import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.repository.AbsenceRepository;
import de.mamakow.dienstplanapotheke.repository.BranchRepository;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;
import de.mamakow.dienstplanapotheke.repository.OvertimeRepository;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.util.Event;
import de.mamakow.dienstplanapotheke.util.UIError;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.TIRAMISU})
public class MainViewModelTest {

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
    private Observer<Event<UIError>> uiErrorObserver;
    @Mock
    private Observer<Boolean> loadingObserver;
    @Captor
    private ArgumentCaptor<Event<UIError>> uiErrorCaptor;

    @Before
    public void setUp() throws Exception {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        viewModel = new MainViewModel(RuntimeEnvironment.getApplication());

        // Inject mocks using reflection because the ViewModel doesn't have a constructor for injection
        injectMock("rosterRepository", rosterRepository);
        injectMock("employeeRepository", employeeRepository);
        injectMock("branchRepository", branchRepository);
        injectMock("absenceRepository", absenceRepository);
        injectMock("overtimeRepository", overtimeRepository);
    }

    @After
    public void tearDown() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    private void injectMock(String fieldName, Object mock) throws Exception {
        Field field = MainViewModel.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(viewModel, mock);
    }

    @Test
    public void refreshData_Error_PostsUIErrorEvent() {
        // Arrange
        String errorMsg = "Verbindungsfehler";

        // Mock rosterRepository.fetchAndSaveRosterData to trigger onError
        doAnswer(invocation -> {
            RetrofitNetworkHandler.NetworkResponseCallback<Void> callback = invocation.getArgument(4);
            callback.onError(errorMsg);
            return null;
        }).when(rosterRepository).fetchAndSaveRosterData(anyString(), anyString(), anyInt(), anyInt(), any());

        viewModel.getUiError().observeForever(uiErrorObserver);

        // Act
        viewModel.refreshData(LocalDate.now(), LocalDate.now().plusDays(7), 1, 1);

        // Assert
        verify(uiErrorObserver).onChanged(uiErrorCaptor.capture());

        Event<UIError> event = uiErrorCaptor.getValue();
        assertNotNull(event);
        UIError error = event.getContentIfNotHandled();
        assertNotNull(error);
        assertEquals(errorMsg, error.getMessage());
        assertEquals(UIError.Type.SNACKBAR_WITH_RETRY, error.getType());
    }

    @Test
    public void refreshData_Success_SetsIsLoadingFalse() {
        // Arrange
        doAnswer(invocation -> {
            RetrofitNetworkHandler.NetworkResponseCallback<Void> callback = invocation.getArgument(4);
            //noinspection ConstantConditions
            callback.onSuccess(null);
            return null;
        }).when(rosterRepository).fetchAndSaveRosterData(anyString(), anyString(), anyInt(), anyInt(), any());

        viewModel.getIsLoading().observeForever(loadingObserver);

        // Act
        viewModel.refreshData(LocalDate.now(), LocalDate.now().plusDays(7), 1, 1);

        // Assert
        // isLoading is initialized to false, then setValue(true), then postValue(false)
        InOrder inOrder = inOrder(loadingObserver);
        inOrder.verify(loadingObserver).onChanged(false); // Initial value
        inOrder.verify(loadingObserver).onChanged(true);  // Start of refreshData
        inOrder.verify(loadingObserver).onChanged(false); // End of refreshData (success)
    }

    @Test
    public void fetchOvertimes_Error_PostsToastError() {
        // Arrange
        String errorMsg = "Fehler beim Laden der Überstunden";
        doAnswer(invocation -> {
            RetrofitNetworkHandler.NetworkResponseCallback<Void> callback = invocation.getArgument(1);
            callback.onError(errorMsg);
            return null;
        }).when(overtimeRepository).fetchAndSaveEmployeeOvertimes(anyInt(), any());

        viewModel.getUiError().observeForever(uiErrorObserver);

        // Act
        viewModel.fetchOvertimes(1);

        // Assert
        verify(uiErrorObserver).onChanged(uiErrorCaptor.capture());

        UIError error = uiErrorCaptor.getValue().getContentIfNotHandled();
        assertNotNull(error);
        assertEquals(errorMsg, error.getMessage());
        assertEquals(UIError.Type.TOAST, error.getType());
    }
}
