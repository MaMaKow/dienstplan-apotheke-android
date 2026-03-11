package de.mamakow.dienstplanapotheke.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.network.NetworkHandler;
import io.github.cdimascio.dotenv.Dotenv;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "session_token";
    private final SharedPreferences sharedPreferences;
    private final NetworkHandler networkHandler;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        networkHandler = new NetworkHandler(context, this);
    }

    public void performLogin() {
        Log.d(TAG, "performLogin gestartet");
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("/assets")
                    .filename("env")
                    .load();

            String userName = dotenv.get("USERNAME");
            String userPassphrase = dotenv.get("PASSPHRASE");

            if (userName == null || userPassphrase == null) {
                Log.e(TAG, "Username oder Passphrase in .env nicht gefunden!");
                return;
            }

            Log.i(TAG, "Versuche Login für User: " + userName);
            networkHandler.login(userName, userPassphrase, new LoginCallback() {
                @Override
                public void onSuccess(String token) {
                    saveToken(token);
                    Log.i(TAG, "Login erfolgreich. Token erhalten und gespeichert.");
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.e(TAG, "Login fehlgeschlagen: " + exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Laden der .env Datei oder beim Login-Prozess", e);
        }
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
        Log.d(TAG, "Token in SharedPreferences gespeichert: " + (token != null ? "vorhanden" : "null"));
    }

    public boolean isNotLoggedIn() {
        String token = getSessionToken();
        boolean loggedIn = token != null && !token.isEmpty();
        Log.d(TAG, "Check Login-Status: " + (loggedIn ? "Eingeloggt" : "NICHT eingeloggt"));
        return !loggedIn;
    }

    public String getSessionToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void logout() {
        Log.d(TAG, "Logout: Lösche Token aus SharedPreferences");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
    }
}
