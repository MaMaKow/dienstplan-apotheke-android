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
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
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
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RetrofitNetworkHandler {

    final String TAG = "RetrofitNetHandler";
    private final RosterApi rosterApi;
    private final Gson gson;

    public RetrofitNetworkHandler(Context context) {
        String apiBaseUrl = context.getString(R.string.api_base_url);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context1) -> {
                    String val = json.getAsString();
                    if (val == null || val.isEmpty() || val.equals("null")) return null;
                    return LocalDate.parse(val, DateTimeFormatter.ISO_LOCAL_DATE);
                })
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context1) -> {
                    String val = json.getAsString();
                    if (val == null || val.isEmpty() || val.equals("null")) return null;
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
        Log.i(TAG, "fetchRoster() gestartet für Zeitraum: " + dateStart + " bis " + dateEnd);

        Call<JsonElement> call = rosterApi.getRoster("Bearer " + token, dateStart, dateEnd, employeeKey);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement body = response.body();

                    if (body.isJsonArray()) {
                        List<RosterItem> allItems = new ArrayList<>();
                        JsonArray daysArray = body.getAsJsonArray();
                        for (JsonElement dayElement : daysArray) {
                            DayWrapper day = gson.fromJson(dayElement, DayWrapper.class);
                            if (day.roster != null) {
                                allItems.addAll(day.roster);
                            }
                        }
                        Log.i(TAG, "Erfolgreich " + allItems.size() + " Einträge extrahiert.");
                        callback.onSuccess(allItems);
                    } else if (body.isJsonObject()) {
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

    public void fetchEmployees(String token, NetworkResponseCallback<List<Employee>> callback) {
        Log.i(TAG, "fetchEmployees() gestartet");
        Call<List<Employee>> call = rosterApi.getEmployees("Bearer " + token);
        call.enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "Mitarbeiter erfolgreich geladen: " + response.body().size());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Fehler Mitarbeiter: " + response.code());
                    callback.onError("Fehler beim Abrufen der Mitarbeiter: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                Log.e(TAG, "Netzwerkfehler Mitarbeiter", t);
                callback.onError("Netzwerkfehler bei fetchEmployees: " + t.getMessage());
            }
        });
    }

    public void fetchBranches(String token, NetworkResponseCallback<List<Branch>> callback) {
        Log.i(TAG, "fetchBranches() gestartet");
        Call<List<Branch>> call = rosterApi.getBranches("Bearer " + token);
        call.enqueue(new Callback<List<Branch>>() {
            @Override
            public void onResponse(Call<List<Branch>> call, Response<List<Branch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "Filialen erfolgreich geladen: " + response.body().size());
                    Log.i(TAG, "Daten: " + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Fehler Filialen: " + response.code() + " " + response.message());
                    callback.onError("Fehler beim Abrufen der Filialen: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Branch>> call, Throwable t) {
                Log.e(TAG, "Netzwerkfehler Filialen", t);
                callback.onError("Netzwerkfehler bei fetchBranches: " + t.getMessage());
            }
        });
    }

    public void fetchBranchById(String token, int branchId, NetworkResponseCallback<Branch> callback) {
        Log.i(TAG, "fetchBranchById() gestartet für ID: " + branchId);
        Call<Branch> call = rosterApi.getBranchById("Bearer " + token, branchId);
        call.enqueue(new Callback<Branch>() {
            @Override
            public void onResponse(Call<Branch> call, Response<Branch> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Fehler beim Abrufen der Filiale " + branchId + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Branch> call, Throwable t) {
                callback.onError("Netzwerkfehler bei fetchBranchById: " + t.getMessage());
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

        @GET("employees")
        Call<List<Employee>> getEmployees(@Header("Authorization") String authorization);

        @GET("branches")
        Call<List<Branch>> getBranches(@Header("Authorization") String authorization);

        @GET("branches/{id}")
        Call<Branch> getBranchById(
                @Header("Authorization") String authorization,
                @Path("id") int branchId
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
