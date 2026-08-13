package de.mamakow.dienstplanapotheke;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import de.mamakow.dienstplanapotheke.session.SessionManager;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

@RunWith(AndroidJUnit4.class)
public class ErrorDisplayUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private MockWebServer mockWebServer;
    private SessionManager sessionManager;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        sessionManager = new SessionManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
        // Setze die API-URL auf den lokalen Mock-Server
        sessionManager.saveBaseUrl(mockWebServer.url("/").toString());
        sessionManager.performLogin("testuser", "password", null);
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testServerError_ShowsSnackbarWithRetry() {
        // Simuliere einen 500er Fehler (OkHttp 4 Syntax)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // Verifiziere, dass die Snackbar erscheint
        onView(withText("Server-Fehler. Bitte versuchen Sie es später erneut."))
                .check(matches(isDisplayed()));

        onView(withText("Wiederholen"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testTimeoutError_ShowsSpecificMessage() {
        // Simuliere einen Timeout (OkHttp 4 Syntax)
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        // Prüfe auf die spezifische Timeout-Nachricht
        onView(withText("Die Verbindung zum Server dauert zu lange. Bitte prüfen Sie Ihr Internet."))
                .check(matches(isDisplayed()));
    }
}
