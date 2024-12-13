package de.mamakow.dienstplanapotheke;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.IOException;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class NetworkHandler {
    private final Context context;
    OkHttpClient client;
    String pdrBaseUrl;
    String restBaseUrl;
    String accessToken;

    // Constructor to initialize OkHttpClient
    public NetworkHandler(Context context) {
        this.context = context;
        client = new OkHttpClient();
        pdrBaseUrl = context.getString(R.string.test_page_url);
        restBaseUrl = pdrBaseUrl + "src/php/restful-api/";


    }

    // Method to perform a GET request
    public void fetchRoster() {
        // POST to login
        Dotenv dotenv = Dotenv.configure()
                .directory("/assets")
                .filename("env") // instead of '.env', use 'env'
                .load();

        String userName = dotenv.get("USERNAME");
        String userPassphrase = dotenv.get("PASSPHRASE");
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    String accessToken = responseBody.string();
                    Log.i("data", responseBody.string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // GET to receive roster data
        Request get = new Request.Builder()
                .url(restBaseUrl + "roster/GET-roster.php")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    Log.i("data", responseBody.string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
