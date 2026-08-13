package de.mamakow.dienstplanapotheke.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.mamakow.dienstplanapotheke.database.AbsenceDao;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class AbsenceRepositoryTest {

    private AbsenceRepository repository;

    @Mock
    private AbsenceDao absenceDao;

    @Mock
    private RetrofitNetworkHandler networkHandler;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private RetrofitNetworkHandler.NetworkResponseCallback<Void> callback;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new AbsenceRepository(absenceDao, networkHandler, sessionManager);
    }

    @Test
    public void fetchAndSaveAbsences_NetworkError_CallsCallbackOnError() {
        // Arrange
        String token = "fake_token";
        String errorMsg = "Netzwerkfehler";
        when(sessionManager.getSessionToken()).thenReturn(token);

        // Mock networkHandler.fetchAbsences to call onError on the passed callback
        doAnswer(invocation -> {
            RetrofitNetworkHandler.NetworkResponseCallback<?> internalCallback = invocation.getArgument(1);
            internalCallback.onError(errorMsg);
            return null;
        }).when(networkHandler).fetchAbsences(eq(token), any());

        // Act
        repository.fetchAndSaveAbsences(callback);

        // Assert
        verify(callback).onError(errorMsg);
    }

    @Test
    public void fetchAndSaveAbsences_NoToken_CallsCallbackOnError() {
        // Arrange
        when(sessionManager.getSessionToken()).thenReturn(null);

        // Act
        repository.fetchAndSaveAbsences(callback);

        // Assert
        verify(sessionManager).performLogin();
        verify(callback).onError("Nicht angemeldet.");
    }
}
