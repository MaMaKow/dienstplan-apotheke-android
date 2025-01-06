package de.mamakow.dienstplanapotheke.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.network.NetworkHandler;
import io.github.cdimascio.dotenv.Dotenv;

public class SessionManager {
    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "session_token";
    private final SharedPreferences sharedPreferences;
    private final NetworkHandler networkHandler;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        networkHandler = new NetworkHandler(context, this);
    }

    public void performLogin() {
        Log.d("SessionManager", "performLogin 0");
        Dotenv dotenv = Dotenv.configure()
                .directory("/assets")
                .filename("env") // instead of '.env', use 'env'
                .load();

        String userName = dotenv.get("USERNAME");
        String userPassphrase = dotenv.get("PASSPHRASE");
        Log.i("SessionManager", "username = " + userName);
        networkHandler.login(userName, userPassphrase, new LoginCallback() {
            @Override
            public void onSuccess(String token) {
                saveToken(token);
                Log.i("SessionManager", "Login successful. Token saved.");
            }

            @Override
            public void onFailure(Exception exception) {
                Log.e("SessionManager", "Login failed.", exception);
                Log.e("SessionManager", exception.getLocalizedMessage(), exception);
                Log.getStackTraceString(exception);
            }
        });
        Log.d("SessionManager", "performLogin 99");

    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
        Log.i("SessionManager", "Token was saved");
        Log.i("SessionManager", token);
    }

    public boolean isNotLoggedIn() {
        String token = sharedPreferences.getString(TOKEN_KEY, null);
        try {
            Log.d("SessionManager", token);
        } catch (Exception exception) {
            Log.d("SessionManager", "Could not print token. Probably it is null.");

            Log.getStackTraceString(exception);
        }
        boolean result = token != null;
        if (result) {
            Log.d("SessionManager", "token is set. We are logged in.");
        } else {
            Log.d("SessionManager", "token is null. We need to log in.");
        }
        return !result;
    }

    public String getSessionToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
    }
}