package de.mamakow.dienstplanapotheke.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.session.SessionManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkHandler {
    private final SessionManager sessionManager;
    private final OkHttpClient client;
    private final String restBaseUrl;

    // Constructor to initialize OkHttpClient and SessionManager
    public NetworkHandler(Context context, SessionManager sessionManager) {
        client = new OkHttpClient();
        this.sessionManager = sessionManager;
        String pdrBaseUrl = context.getString(R.string.test_page_url);
        restBaseUrl = pdrBaseUrl + "src/php/restful-api/";
    }

    // Method to perform a GET request
    public void fetchRoster() {
        String token = sessionManager.getSessionToken();
        if (sessionManager.isNotLoggedIn()) {
            Log.e("NetworkHandler", "User not logged in");
        }


        // GET to receive roster data
        Request get = new Request.Builder()
                .url(restBaseUrl + "roster/GET-roster.php")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException exception) {
                Log.getStackTraceString(exception);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        // Handle token expiration
                        if (response.code() == 401) {
                            Log.e("NetworkHandler", "Token expired. User needs to log in again.");
                            // Implement token refresh logic or prompt user to log in again
                        } else {
                            throw new IOException("Unexpected code " + response);
                        }
                    } else {
                        Log.i("data", responseBody.string());
                    }
                } catch (Exception exception) {
                    Log.getStackTraceString(exception);
                }
            }
        });
    }


    public void login(String userName, String userPassphrase, LoginCallback callback) {
        JsonObject postData = new JsonObject();
        postData.addProperty("userName", userName);
        postData.addProperty("userPassphrase", userPassphrase);

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody postBody = RequestBody.create(postData.toString(), JSON);
        Request post = new Request.Builder()
                .url(restBaseUrl + "authentication/POST-authenticate.php")
                .post(postBody)
                .build();

        client.newCall(post).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    String token = responseBody.string();
                    callback.onSuccess(token);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });
    }
}