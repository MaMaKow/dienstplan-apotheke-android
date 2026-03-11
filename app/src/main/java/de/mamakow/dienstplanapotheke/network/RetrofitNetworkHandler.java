package de.mamakow.dienstplanapotheke.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public class RetrofitNetworkHandler {

    final String TAG = "RetrofitNetHandler";
    private final RosterApi rosterApi;
    private final Gson gson;

    public RetrofitNetworkHandler(Context context) {
        String apiBaseUrl = context.getString(R.string.api_base_url);

        // Logging für HTTP Requests hinzufügen
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context1) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context1) -> {
                    String val = json.getAsString();
                    // Unterstützung für verschiedene ISO Formate
                    return LocalDateTime.parse(val, DateTimeFormatter.ISO_DATE_TIME);
                })
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        rosterApi = retrofit.create(RosterApi.class);
    }

    public void fetchRoster(String token, String dateStart, String dateEnd, Integer employeeKey, NetworkResponseCallback<List<RosterItem>> callback) {
        Log.d(TAG, "fetchRoster() gestartet für Zeitraum: " + dateStart + " bis " + dateEnd);

        // Wir verwenden JsonElement, um sowohl Arrays (Erfolg) als auch Objekte (Fehler wie "Token expired") zu handhaben
        Call<JsonElement> call = rosterApi.getRoster("Bearer " + token, dateStart, dateEnd, employeeKey);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement body = response.body();

                    if (body.isJsonArray()) {
                        // Erfolgsfall: Wir haben eine Liste von Tagen erhalten
                        List<RosterItem> allItems = new ArrayList<>();
                        JsonArray daysArray = body.getAsJsonArray();
                        for (JsonElement dayElement : daysArray) {
                            DayWrapper day = gson.fromJson(dayElement, DayWrapper.class);
                            if (day.roster != null) {
                                allItems.addAll(day.roster);
                            }
                        }
                        Log.d(TAG, "Erfolgreich " + allItems.size() + " Einträge extrahiert.");
                        callback.onSuccess(allItems);
                    } else if (body.isJsonObject()) {
                        // Möglicher Fehlerfall im Body bei 200 OK (z.B. {"error": "Token expired"})
                        JsonObject obj = body.getAsJsonObject();
                        if (obj.has("error")) {
                            String errorMsg = obj.get("error").getAsString();
                            Log.e(TAG, "API Fehler erhalten: " + errorMsg);
                            callback.onError(errorMsg);
                        } else {
                            callback.onError("Unerwartetes JSON-Objekt erhalten");
                        }
                    } else {
                        callback.onError("Unerwartetes Antwortformat");
                    }
                } else {
                    String errorMsg = "Fehler " + response.code() + ": " + response.message();
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e(TAG, "Netzwerkfehler in fetchRoster", t);
                callback.onError("Netzwerkfehler: " + t.getMessage());
            }
        });
    }

    private interface RosterApi {
        @GET("rosters")
        Call<JsonElement> getRoster(
                @Header("Authorization") String authorization,
                @Query("dateStart") String dateStart,
                @Query("dateEnd") String dateEnd,
                @Query("employeeKey") Integer employeeKey
        );
    }

    public interface NetworkResponseCallback<T> {
        void onSuccess(T data);

        void onError(String errorMessage);
    }

    private static class DayWrapper {
        String date;
        List<RosterItem> roster;
    }
}
