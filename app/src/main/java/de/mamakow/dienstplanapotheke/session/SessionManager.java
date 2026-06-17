package de.mamakow.dienstplanapotheke.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Privileges;
import de.mamakow.dienstplanapotheke.model.UserData;
import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.network.NetworkHandler;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "session_token";
    private static final String BASE_URL_KEY = "base_url";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_DISPLAY_NAME_KEY = "user_display_name";
    private static final String USER_EMAIL_KEY = "user_email";
    private static final String USER_EMPLOYEE_KEY = "user_employee_key";
    private static final String USER_PRIVILEGES_KEY = "user_privileges";
    private final SharedPreferences sharedPreferences;
    private final NetworkHandler networkHandler;
    private final Context context;
    private boolean loginIsRunning = false;

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
        synchronized (this) {
            if (loginIsRunning) {
                return;
            }
        }
        loginIsRunning = true;
        Log.d(TAG, "performLogin gestartet für User: " + userName);
        networkHandler.login(userName, userPassphrase, new LoginCallback() {
            @Override
            public void onSuccess(String token) {
                loginIsRunning = false;
                saveToken(token);
                saveCredentials(userName, userPassphrase);
                Log.i(TAG, "Login erfolgreich. Token erhalten und gespeichert. Lade Benutzerdaten...");

                // Nach erfolgreichem Login: Benutzerdaten laden
                fetchAndSaveCurrentUser(token, callback);
                Log.i(TAG, "Benutzerdaten vermutlich erfolgreich geladen und gespeichert.");
            }

            @Override
            public void onFailure(Exception exception) {
                loginIsRunning = false;
                Log.e(TAG, "Login fehlgeschlagen: " + exception.getMessage(), exception);
                if (callback != null) callback.onFailure(exception);
            }
        });
    }

    public void fetchAndSaveCurrentUser(String token, LoginCallback callback) {
        Log.d(TAG, "Starte fetchAndSaveCurrentUser...");
        RetrofitNetworkHandler retrofitHandler = new RetrofitNetworkHandler(context);
        retrofitHandler.fetchCurrentUser(token, new RetrofitNetworkHandler.NetworkResponseCallback<UserData>() {
            @Override
            public void onSuccess(UserData userData) {
                Log.d(TAG, "Benutzerdaten erfolgreich geladen.");
                saveFullUserData(userData);
                Log.i(TAG, "Benutzerdaten erfolgreich geladen und gespeichert.");
                if (callback != null) callback.onSuccess(token);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Fehler beim Laden der Benutzerdaten: " + errorMessage);
                // Auch wenn UserData fehlschlägt, haben wir den Token, also Login war technisch erfolgreich.
                // Aber wir rufen callback.onSuccess auf, damit die App weitermachen kann.
                if (callback != null) callback.onSuccess(token);
            }
        });
    }

    private void saveCredentials(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
    }

    public void saveUserData(int userId, String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(USER_ID_KEY, userId);
        editor.putString(USER_DISPLAY_NAME_KEY, userName);
        editor.apply();
        Log.d(TAG, "User-Daten gespeichert (Legacy): ID=" + userId + ", Name=" + userName);
    }

    public void saveFullUserData(UserData userData) {
        Log.d(TAG, "Starte saveFullUserData...");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(USER_ID_KEY, userData.getId());
        editor.putString(USER_DISPLAY_NAME_KEY, userData.getUserName());
        editor.putString(USER_EMAIL_KEY, userData.getEmail());
        editor.putInt(USER_EMPLOYEE_KEY, userData.getEmployeeKey() != null ? userData.getEmployeeKey() : -1);
        if (userData.getPrivileges() != null) {
            String privilegesJson = new Gson().toJson(userData.getPrivileges());
            editor.putString(USER_PRIVILEGES_KEY, privilegesJson);
        }
        editor.apply();
        Log.d(TAG, "Vollständige User-Daten gespeichert user name: " + userData.getUserName());
        Log.d(TAG, "employee key: " + userData.getEmployeeKey());
        Log.d(TAG, "email: " + userData.getEmail());
        Log.d(TAG, "userId: " + userData.getId());
        Log.d(TAG, "Privileges" + userData.getPrivileges());
    }

    public int getUserId() {
        return sharedPreferences.getInt(USER_ID_KEY, -1);
    }

    public int getUserEmployeeKey() {
        return sharedPreferences.getInt(USER_EMPLOYEE_KEY, -1);
    }

    public String getUserDisplayName() {
        return sharedPreferences.getString(USER_DISPLAY_NAME_KEY, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(USER_EMAIL_KEY, "");
    }

    public Privileges getUserPrivileges() {
        String json = sharedPreferences.getString(USER_PRIVILEGES_KEY, null);
        if (json == null) return new Privileges(); // oder null
        return new Gson().fromJson(json, Privileges.class);
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.commit(); // Immediate commit to storage, not using .apply()!
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, "Token in SharedPreferences gespeichert: " + (token != null ? "vorhanden" : "null"));
        Log.d(TAG, "Token:" + token);
        logTokenContent(sharedPreferences.getString(TOKEN_KEY, null));
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
        Log.d(TAG, ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");


    }

    public boolean isNotLoggedIn() {
        String token = getSessionToken();
        Log.d(TAG, "Token: " + token);
        logTokenContent(token);
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
        if (url.trim().isEmpty()) {
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
        editor.remove(USER_ID_KEY);
        editor.remove(USER_DISPLAY_NAME_KEY);
        editor.remove(USER_EMAIL_KEY);
        editor.remove(USER_PRIVILEGES_KEY);
        editor.apply();
    }

    /**
     * Lädt die Benutzerdaten basierend auf dem gespeicherten Token nach.
     */
    public void refreshSessionUserData(LoginCallback callback) {
        String token = getSessionToken();
        if (token != null) {
            fetchAndSaveCurrentUser(token, callback);
        } else {
            if (callback != null) callback.onFailure(new Exception("Kein Token vorhanden"));
        }
    }

    public boolean hasUserData() {
        // Prüfen, ob wir eine gültige User-ID oder einen EmployeeKey haben
        return sharedPreferences.getInt(USER_ID_KEY, -1) != -1;
    }

    private void logTokenContent(String token) {
        if (token == null || !token.contains(".")) {
            Log.w(TAG, "Token ist null oder hat kein gültiges JWT Format.");
            return;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return;

            // Payload dekodieren
            byte[] decodedBytes = Base64.decode(parts[1], Base64.URL_SAFE);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            Log.d(TAG, "Token Payload Rohdaten: " + decodedString);

            // JSON parsen
            JsonObject jsonObject = new Gson().fromJson(decodedString, JsonObject.class);

            // Sicher auslesen mit Hilfsvariablen
            if (jsonObject.has("userName")) {
                Log.d(TAG, "Username: " + jsonObject.get("userName").getAsString());
            }

            if (jsonObject.has("userPrimaryKey")) {
                Log.d(TAG, "User ID: " + jsonObject.get("userPrimaryKey").getAsInt());
            }

            if (jsonObject.has("exp")) {
                long expiresSeconds = jsonObject.get("exp").getAsLong();

                // Zeitstempel umwandeln (mit ZoneOffset System Default)
                java.time.Instant instant = java.time.Instant.ofEpochSecond(expiresSeconds);
                java.time.LocalDateTime expiresDateTime = java.time.LocalDateTime.ofInstant(
                        instant, java.time.ZoneId.systemDefault());

                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                Log.d(TAG, "Ablaufdatum (lokal): " + expiresDateTime.format(formatter));

                // Prüfen ob abgelaufen
                if (expiresDateTime.isBefore(java.time.LocalDateTime.now())) {
                    Log.w(TAG, "WARNUNG: Der Token ist bereits abgelaufen!");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Analysieren des Tokens: " + e.getMessage());
        }
    }
}
