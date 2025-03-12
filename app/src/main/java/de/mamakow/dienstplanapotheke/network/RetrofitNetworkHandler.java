package de.mamakow.dienstplanapotheke.network;

import android.content.Context;

import de.mamakow.dienstplanapotheke.R;
import de.mamakow.dienstplanapotheke.model.Roster;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class RetrofitNetworkHandler {

    private final RosterApi rosterApi;

    public RetrofitNetworkHandler(Context context) {
        String pdrBaseUrl = context.getString(R.string.test_page_url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(pdrBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        rosterApi = retrofit.create(RosterApi.class);
    }

    public void fetchRoster(String token, NetworkResponseCallback<Roster> callback) {
        Call<Roster> call = rosterApi.getRoster("Bearer " + token);
        call.enqueue(new Callback<Roster>() {
            @Override
            public void onResponse(Call<Roster> call, Response<Roster> response) {
                if (response.isSuccessful()) {
                    Roster roster = response.body();
                    callback.onSuccess(roster);
                } else {
                    callback.onError("Fehler: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Roster> call, Throwable t) {
                callback.onError("Netzwerkfehler: " + t.getMessage());
            }
        });
    }

    public interface RosterApi {
        @GET("roster/GET-roster.php")
        Call<Roster> getRoster(@Header("Authorization") String authorization);
    }

    public interface NetworkResponseCallback<T> {
        void onSuccess(T data);

        void onError(String errorMessage);
    }
}