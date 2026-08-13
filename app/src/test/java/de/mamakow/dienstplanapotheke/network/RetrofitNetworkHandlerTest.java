package de.mamakow.dienstplanapotheke.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.TIRAMISU})
public class RetrofitNetworkHandlerTest {

    private RetrofitNetworkHandler networkHandler;

    @Before
    public void setUp() {
        networkHandler = new RetrofitNetworkHandler(RuntimeEnvironment.getApplication());
    }

    @Test
    public void testMapThrowableToMessage_Timeout() {
        Throwable timeout = new SocketTimeoutException();
        String message = networkHandler.mapThrowableToMessage(timeout);
        assertEquals("Die Verbindung zum Server dauert zu lange. Bitte prüfen Sie Ihr Internet.", message);
    }

    @Test
    public void testMapThrowableToMessage_NoInternet() {
        Throwable noInternet = new UnknownHostException();
        String message = networkHandler.mapThrowableToMessage(noInternet);
        assertEquals("Keine Internetverbindung verfügbar.", message);
    }

    @Test
    public void testMapThrowableToMessage_Unknown() {
        Throwable error = new RuntimeException("Generic Error");
        String message = networkHandler.mapThrowableToMessage(error);
        assertTrue(message.contains("Netzwerkfehler"));
        assertTrue(message.contains("Generic Error"));
    }
}
