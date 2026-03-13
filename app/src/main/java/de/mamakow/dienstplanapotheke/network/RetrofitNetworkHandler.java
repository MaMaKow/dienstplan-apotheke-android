package de.mamakow.dienstplanapotheke.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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

    private <T> void handleListResponse(Response<JsonElement> response, Type listType, NetworkResponseCallback<List<T>> callback) {
        if (response.isSuccessful() && response.body() != null) {
            JsonElement body = response.body();
            if (body.isJsonArray()) {
                List<T> data = gson.fromJson(body, listType);
                callback.onSuccess(data);
            } else if (body.isJsonObject()) {
                JsonObject obj = body.getAsJsonObject();
                if (obj.has("error")) {
                    callback.onError(obj.get("error").getAsString());
                } else {
                    callback.onError("Unerwartetes JSON-Objekt");
                }
            } else {
                callback.onError("Unerwartetes Format");
            }
        } else {
            callback.onError("Fehler " + response.code());
        }
    }

    private <T> void handleSingleResponse(Response<JsonElement> response, Type type, NetworkResponseCallback<T> callback) {
        if (response.isSuccessful() && response.body() != null) {
            JsonElement body = response.body();
            if (body.isJsonObject()) {
                JsonObject obj = body.getAsJsonObject();
                if (obj.has("error")) {
                    callback.onError(obj.get("error").getAsString());
                } else {
                    T data = gson.fromJson(body, type);
                    callback.onSuccess(data);
                }
            } else {
                callback.onError("Unerwartetes Format");
            }
        } else {
            callback.onError("Fehler " + response.code());
        }
    }

    public void fetchRoster(String token, String dateStart, String dateEnd, Integer employeeKey, Integer branchId, NetworkResponseCallback<List<RosterItem>> callback) {
        Log.i(TAG, "fetchRoster() gestartet: " + dateStart + " bis " + dateEnd);
        rosterApi.getRoster("Bearer " + token, dateStart, dateEnd, employeeKey, branchId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isJsonArray()) {
                    List<RosterItem> allItems = new ArrayList<>();
                    for (JsonElement dayElement : response.body().getAsJsonArray()) {
                        DayWrapper day = gson.fromJson(dayElement, DayWrapper.class);
                        if (day.roster != null) allItems.addAll(day.roster);
                    }
                    callback.onSuccess(allItems);
                } else {
                    handleListResponse(response, new TypeToken<List<RosterItem>>() {
                    }.getType(), callback);
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void fetchEmployees(String token, NetworkResponseCallback<List<Employee>> callback) {
        Log.i(TAG, "fetchEmployees() gestartet");
        rosterApi.getEmployees("Bearer " + token).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Employee>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void fetchBranches(String token, NetworkResponseCallback<List<Branch>> callback) {
        Log.i(TAG, "fetchBranches() gestartet");
        rosterApi.getBranches("Bearer " + token).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Branch>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void fetchBranchById(String token, int branchId, NetworkResponseCallback<Branch> callback) {
        Log.i(TAG, "fetchBranchById() gestartet für ID: " + branchId);
        rosterApi.getBranchById("Bearer " + token, branchId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                handleSingleResponse(response, Branch.class, callback);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private interface RosterApi {
        @GET("rosters")
        Call<JsonElement> getRoster(@Header("Authorization") String auth, @Query("dateStart") String s, @Query("dateEnd") String e, @Query("employeeKey") Integer ek, @Query("branchId") Integer bi);

        @GET("employees")
        Call<JsonElement> getEmployees(@Header("Authorization") String auth);

        @GET("branches")
        Call<JsonElement> getBranches(@Header("Authorization") String auth);

        @GET("branches/{id}")
        Call<JsonElement> getBranchById(@Header("Authorization") String authorization, @Path("id") int branchId);
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
