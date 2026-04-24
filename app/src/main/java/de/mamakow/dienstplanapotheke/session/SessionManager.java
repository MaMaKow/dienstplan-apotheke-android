package de.mamakow.dienstplanapotheke.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.network.NetworkHandler;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "session_token";
    private static final String BASE_URL_KEY = "base_url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    private final SharedPreferences sharedPreferences;
    private final NetworkHandler networkHandler;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        networkHandler = new NetworkHandler(context, this);
    }

    public void performLogin() {
        String username = sharedPreferences.getString(USERNAME_KEY, null);
        String password = sharedPreferences.getString(PASSWORD_KEY, null);

        if (username != null && password != null) {
            performLogin(username, password, null);
        } else {
            Log.e(TAG, "No credentials stored for automatic login.");
        }
    }

    public void performLogin(String userName, String userPassphrase, LoginCallback callback) {
        Log.d(TAG, "performLogin gestartet für User: " + userName);
        networkHandler.login(userName, userPassphrase, new LoginCallback() {
            @Override
            public void onSuccess(String token) {
                saveToken(token);
                saveCredentials(userName, userPassphrase);
                Log.i(TAG, "Login erfolgreich. Token erhalten und gespeichert.");
                if (callback != null) callback.onSuccess(token);
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e(TAG, "Login fehlgeschlagen: " + exception.getMessage(), exception);
                if (callback != null) callback.onFailure(exception);
            }
        });
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
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

    public void saveBaseUrl(String url) {
        if (url != null && !url.trim().isEmpty() && !url.endsWith("/")) {
            url += "/";
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BASE_URL_KEY, url);
        editor.apply();
    }

    public String getBaseUrl() {
        String url = sharedPreferences.getString(BASE_URL_KEY, "");
        if (url == null || url.trim().isEmpty()) {
            return context.getString(R.string.test_page_url);
        }
        return url;
    }

    public boolean isBaseUrlSet() {
        String url = sharedPreferences.getString(BASE_URL_KEY, null);
        return url != null && !url.trim().isEmpty();
    }

    public String getApiBaseUrl() {
        return getBaseUrl() + "src/php/restful-api/";
    }

    public void logout() {
        Log.d(TAG, "Logout: Lösche Token und Credentials aus SharedPreferences");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.remove(USERNAME_KEY);
        editor.remove(PASSWORD_KEY);
        editor.apply();
    }
}
