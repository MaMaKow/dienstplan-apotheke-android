package de.mamakow.dienstplanapotheke.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import de.mamakow.dienstplanapotheke.database.RosterItemDao;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class RosterRepositoryTest {

    private RosterRepository repository;

    @Mock
    private RetrofitNetworkHandler networkHandler;
    @Mock
    private RosterItemDao rosterItemDao;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private RetrofitNetworkHandler.NetworkResponseCallback<Void> callback;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        repository = new RosterRepository(networkHandler, rosterItemDao, sessionManager);

        // Inject a synchronous executor for testing
        Executor synchronousExecutor = Runnable::run;
        Field executorField = RosterRepository.class.getDeclaredField("executor");
        executorField.setAccessible(true);
        executorField.set(repository, synchronousExecutor);
    }

    @Test
    public void fetchAndSaveRosterData_NoToken_CallsError() {
        // Arrange
        when(sessionManager.getSessionToken()).thenReturn(null);

        // Act
        repository.fetchAndSaveRosterData("2024-01-01", "2024-01-07", 1, null, true, callback);

        // Assert
        verify(callback).onError("Token is null");
        verify(sessionManager).performLogin();
    }

    @Test
    public void fetchAndSaveRosterData_Success_CallsOnSuccess() {
        // Arrange
        String token = "valid_token";
        when(sessionManager.getSessionToken()).thenReturn(token);

        doAnswer(invocation -> {
            RetrofitNetworkHandler.NetworkResponseCallback<List<RosterItem>> internalCallback = invocation.getArgument(5);
            internalCallback.onSuccess(new ArrayList<>());
            return null;
        }).when(networkHandler).fetchRoster(eq(token), anyString(), anyString(), any(), any(), any());

        // Act
        repository.fetchAndSaveRosterData("2024-01-01", "2024-01-07", 1, null, true, callback);

        // Assert
        verify(callback).onSuccess(null);
    }

    @Test
    public void fetchAndSaveRosterData_Error_CallsOnError() {
        // Arrange
        String token = "valid_token";
        String error = "Network Error";
        when(sessionManager.getSessionToken()).thenReturn(token);

        doAnswer(invocation -> {
            RetrofitNetworkHandler.NetworkResponseCallback<List<RosterItem>> internalCallback = invocation.getArgument(5);
            internalCallback.onError(error);
            return null;
        }).when(networkHandler).fetchRoster(eq(token), anyString(), anyString(), any(), any(), any());

        // Act
        repository.fetchAndSaveRosterData("2024-01-01", "2024-01-07", 1, null, true, callback);

        // Assert
        verify(callback).onError(error);
    }
}
