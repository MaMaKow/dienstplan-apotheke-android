package de.mamakow.dienstplanapotheke.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.Map;

import de.mamakow.dienstplanapotheke.model.Absence;
import de.mamakow.dienstplanapotheke.model.Branch;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Overtime;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.model.UserData;
import de.mamakow.dienstplanapotheke.model.dto.RosterItemDto;
import de.mamakow.dienstplanapotheke.session.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitNetworkHandler {

    private static final String TAG = "RetrofitNetHandler";
    private final RosterApi rosterApi;
    private final Gson gson;
    private final SessionManager sessionManager;

    public RetrofitNetworkHandler(@NonNull Context context) {
        sessionManager = new SessionManager(context);
        String apiBaseUrl = sessionManager.getApiBaseUrl();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    String token = sessionManager.getSessionToken();
                    okhttp3.Request.Builder builder = chain.request().newBuilder();

                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    return chain.proceed(builder.build());
                })
                .build();

        DateTimeFormatter apiDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context1) -> {
                    String val = json.getAsString();
                    if (val == null || val.isEmpty() || val.equals("null")) return null;
                    if (val.length() > 10) {
                        return LocalDate.parse(val, apiDateTimeFormatter);
                    }
                    return LocalDate.parse(val, DateTimeFormatter.ISO_LOCAL_DATE);
                })
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context1) -> {
                    String val = json.getAsString();
                    if (val == null || val.isEmpty() || val.equals("null")) return null;
                    if (val.contains(" ")) {
                        return LocalDateTime.parse(val, apiDateTimeFormatter);
                    }
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

    private <T> void handleListResponse(@NonNull Response<JsonElement> response, @NonNull Type listType, @NonNull NetworkResponseCallback<List<T>> callback) {
        if (response.code() == 401) {
            Log.w(TAG, "401 Unauthorized - Starte automatischen Re-Login");
            sessionManager.performLogin();
            callback.onError("Sitzung abgelaufen. Bitte melden Sie sich erneut an.");
            return;
        }
        if (response.isSuccessful() && response.body() != null) {
            JsonElement body = response.body();
            if (body.isJsonArray()) {
                List<T> data = gson.fromJson(body, listType);
                if (data != null) {
                    callback.onSuccess(data);
                } else {
                    callback.onError("Fehler beim Verarbeiten der Daten.");
                }
            } else if (body.isJsonObject()) {
                JsonObject obj = body.getAsJsonObject();
                if (obj.has("error")) {
                    callback.onError(obj.get("error").getAsString());
                } else {
                    callback.onError("Unerwartetes Antwortformat vom Server.");
                }
            } else {
                callback.onError("Unerwartetes Format.");
            }
        } else {
            callback.onError(mapErrorCodeToMessage(response.code()));
        }
    }

    private <T> void handleSingleResponse(@NonNull Response<JsonElement> response, @NonNull Type type, @NonNull NetworkResponseCallback<T> callback) {
        if (response.code() == 401) {
            sessionManager.performLogin();
            callback.onError("Sitzung abgelaufen. Bitte melden Sie sich erneut an.");
            return;
        }
        if (response.isSuccessful() && response.body() != null) {
            JsonElement body = response.body();
            if (body.isJsonObject()) {
                JsonObject obj = body.getAsJsonObject();
                if (obj.has("error")) {
                    callback.onError(obj.get("error").getAsString());
                } else {
                    T data = gson.fromJson(body, type);
                    if (data != null) {
                        callback.onSuccess(data);
                    } else {
                        callback.onError("Fehler beim Verarbeiten der Daten.");
                    }
                }
            } else {
                callback.onError("Unerwartetes Format.");
            }
        } else {
            callback.onError(mapErrorCodeToMessage(response.code()));
        }
    }

    public String mapErrorCodeToMessage(int code) {
        return switch (code) {
            case 404 -> "Die angeforderten Daten konnten nicht gefunden werden.";
            case 500 -> "Server-Fehler. Bitte versuchen Sie es später erneut.";
            case 503 -> "Der Server ist aktuell nicht erreichbar.";
            default -> "Ein unerwarteter Fehler ist aufgetreten (Fehler " + code + ").";
        };
    }

    public String mapThrowableToMessage(Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return "Die Verbindung zum Server dauert zu lange. Bitte prüfen Sie Ihr Internet.";
        } else if (t instanceof java.net.UnknownHostException) {
            return "Keine Internetverbindung verfügbar.";
        } else {
            return "Netzwerkfehler: " + (t.getMessage() != null ? t.getMessage() : "Unbekannt");
        }
    }

    public void fetchRoster(@NonNull String token, @NonNull String dateStart, @NonNull String dateEnd, @Nullable Integer employeeKey, @Nullable Integer branchId, @NonNull NetworkResponseCallback<List<RosterItem>> callback) {
        Log.i(TAG, "fetchRoster() gestartet: " + dateStart + " bis " + dateEnd);
        rosterApi.getRoster("Bearer " + token, dateStart, dateEnd, employeeKey, branchId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                JsonElement body = response.body();
                if (response.isSuccessful() && body != null && body.isJsonArray()) {
                    List<RosterItem> allItems = new ArrayList<>();
                    for (JsonElement dayElement : body.getAsJsonArray()) {
                        DayWrapper day = gson.fromJson(dayElement, DayWrapper.class);
                        if (day != null && day.roster != null) allItems.addAll(day.roster);
                    }
                    callback.onSuccess(allItems);
                } else {
                    handleListResponse(response, new TypeToken<List<RosterItem>>() {
                    }.getType(), callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void updateRoster(int branchId, @NonNull String dateStart, @NonNull String dateEnd, @NonNull Map<String, List<RosterItemDto>> data, @NonNull NetworkResponseCallback<String> callback) {
        RosterUpdateRequest request = new RosterUpdateRequest(data);
        rosterApi.updateRoster(branchId, dateStart, dateEnd, request).enqueue(new Callback<RosterUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<RosterUpdateResponse> call, @NonNull Response<RosterUpdateResponse> response) {
                RosterUpdateResponse body = response.body();
                if (response.isSuccessful() && body != null && body.message != null) {
                    callback.onSuccess(body.message);
                } else {
                    callback.onError(mapErrorCodeToMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RosterUpdateResponse> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void deleteRoster(int branchId, @NonNull String date, @NonNull NetworkResponseCallback<String> callback) {
        rosterApi.deleteRoster(branchId, date).enqueue(new Callback<RosterUpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<RosterUpdateResponse> call, @NonNull Response<RosterUpdateResponse> response) {
                RosterUpdateResponse body = response.body();
                if (response.isSuccessful() && body != null && body.message != null) {
                    callback.onSuccess(body.message);
                } else {
                    callback.onError(mapErrorCodeToMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RosterUpdateResponse> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchEmployees(@NonNull String token, @NonNull NetworkResponseCallback<List<Employee>> callback) {
        Log.i(TAG, "fetchEmployees() gestartet");
        rosterApi.getEmployees("Bearer " + token).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Employee>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchBranches(@NonNull String token, @NonNull NetworkResponseCallback<List<Branch>> callback) {
        Log.i(TAG, "fetchBranches() gestartet");
        rosterApi.getBranches("Bearer " + token).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Branch>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchBranchById(@NonNull String token, int branchId, @NonNull NetworkResponseCallback<Branch> callback) {
        Log.i(TAG, "fetchBranchById() gestartet für ID: " + branchId);
        rosterApi.getBranchById("Bearer " + token, branchId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleSingleResponse(response, Branch.class, callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchAbsences(@NonNull String token, @NonNull NetworkResponseCallback<List<Absence>> callback) {
        Log.i(TAG, "fetchAbsences() gestartet");
        rosterApi.getAllAbsences("Bearer " + token).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Absence>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchEmployeeOvertimes(int employeeKey, @NonNull String token, @NonNull NetworkResponseCallback<List<Overtime>> callback) {
        Log.i(TAG, "fetchOvertimes() gestartet");
        rosterApi.getEmployeeOvertimes("Bearer " + token, employeeKey).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Overtime>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchAbsencesByYear(@NonNull String token, int year, @NonNull NetworkResponseCallback<List<Absence>> callback) {
        Log.i(TAG, "fetchAbsencesByYear() gestartet für Jahr: " + year);
        rosterApi.getAbsencesByYear("Bearer " + token, year).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Absence>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchEmployeeAbsences(@NonNull String token, int employeeKey, @Nullable Integer year, @NonNull NetworkResponseCallback<List<Absence>> callback) {
        Log.i(TAG, "fetchEmployeeAbsences() gestartet für Mitarbeiter: " + employeeKey + " Jahr: " + year);
        rosterApi.getEmployeeAbsences("Bearer " + token, employeeKey, year).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Absence>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchEmployeeOvertimes(@NonNull String token, int employeeKey, @NonNull NetworkResponseCallback<List<Overtime>> callback) {
        Log.i(TAG, "fetchEmployeeOvertimes() gestartet für Mitarbeiter: " + employeeKey);
        rosterApi.getEmployeeOvertimes("Bearer " + token, employeeKey).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleListResponse(response, new TypeToken<List<Overtime>>() {
                }.getType(), callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }

    public void fetchCurrentUser(@NonNull String token, @NonNull NetworkResponseCallback<UserData> callback) {
        Log.i(TAG, "fetchCurrentUser() gestartet");
        rosterApi.getCurrentUser("Bearer " + token).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                handleSingleResponse(response, UserData.class, callback);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                callback.onError(mapThrowableToMessage(t));
            }
        });
    }


    public interface NetworkResponseCallback<T> {
        void onSuccess(@NonNull T data);

        void onError(@NonNull String errorMessage);
    }

    private static class DayWrapper {
        @Nullable
        List<RosterItem> roster;
    }

    public static class RosterUpdateResponse {
        @Nullable
        public String message;
    }

    public static class RosterUpdateRequest {
        @NonNull
        public final Map<String, List<RosterItemDto>> data;

        public RosterUpdateRequest(@NonNull Map<String, List<RosterItemDto>> data) {
            this.data = data;
        }
    }
}
