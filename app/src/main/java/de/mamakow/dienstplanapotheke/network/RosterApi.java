package de.mamakow.dienstplanapotheke.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RosterApi {

    @GET("rosters")
    Call<JsonElement> getRoster(
            @Header("Authorization") @NonNull String auth,
            @Query("dateStart") @NonNull String s,
            @Query("dateEnd") @NonNull String e,
            @Query("employeeKey") @Nullable Integer ek,
            @Query("branchId") @Nullable Integer bi
    );

    @PUT("rosters/{branch_id}/{date_start}/{date_end}")
    Call<RetrofitNetworkHandler.RosterUpdateResponse> updateRoster(
            @Path("branch_id") int branchId,
            @Path("date_start") @NonNull String dateStart,
            @Path("date_end") @NonNull String dateEnd,
            @Body @NonNull RetrofitNetworkHandler.RosterUpdateRequest request
    );

    @DELETE("rosters/{branch_id}/{date}")
    Call<RetrofitNetworkHandler.RosterUpdateResponse> deleteRoster(
            @Path("branch_id") int branchId,
            @Path("date") @NonNull String date
    );

    @GET("employees")
    Call<JsonElement> getEmployees(@Header("Authorization") @NonNull String auth);

    @GET("branches")
    Call<JsonElement> getBranches(@Header("Authorization") @NonNull String auth);

    @GET("branches/{id}")
    Call<JsonElement> getBranchById(
            @Header("Authorization") @NonNull String authorization,
            @Path("id") int branchId
    );

    @GET("absences")
    Call<JsonElement> getAllAbsences(@Header("Authorization") @NonNull String auth);

    @GET("absences/{year}")
    Call<JsonElement> getAbsencesByYear(
            @Header("Authorization") @NonNull String auth,
            @Path("year") int year
    );

    @GET("employees/{id}/absences/{year}")
    Call<JsonElement> getEmployeeAbsences(
            @Header("Authorization") @NonNull String auth,
            @Path("id") int employeeKey,
            @Path("year") @Nullable Integer year
    );

    @GET("employees/{id}/overtimes")
    Call<JsonElement> getEmployeeOvertimes(
            @Header("Authorization") @NonNull String auth,
            @Path("id") int employeeKey
    );

    @GET("users/me")
    Call<JsonElement> getCurrentUser(@Header("Authorization") @NonNull String auth);
}
