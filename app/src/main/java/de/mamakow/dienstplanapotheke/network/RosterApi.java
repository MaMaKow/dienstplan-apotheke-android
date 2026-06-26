package de.mamakow.dienstplanapotheke.network;

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
    Call<JsonElement> getRoster(@Header("Authorization") String auth, @Query("dateStart") String s, @Query("dateEnd") String e, @Query("employeeKey") Integer ek, @Query("branchId") Integer bi);

    @PUT("rosters/{branch_id}/{date_start}/{date_end}")
    Call<RetrofitNetworkHandler.RosterUpdateResponse> updateRoster(
            @Path("branch_id") int branchId,
            @Path("date_start") String dateStart,
            @Path("date_end") String dateEnd,
            @Body RetrofitNetworkHandler.RosterUpdateRequest request
    );

    @DELETE("rosters/{branch_id}/{date}")
    Call<RetrofitNetworkHandler.RosterUpdateResponse> deleteRoster(
            @Path("branch_id") int branchId,
            @Path("date") String date
    );

    @GET("employees")
    Call<JsonElement> getEmployees(@Header("Authorization") String auth);

    @GET("branches")
    Call<JsonElement> getBranches(@Header("Authorization") String auth);

    @GET("branches/{id}")
    Call<JsonElement> getBranchById(@Header("Authorization") String authorization, @Path("id") int branchId);

    @GET("absences")
    Call<JsonElement> getAllAbsences(@Header("Authorization") String auth);

    @GET("absences/{year}")
    Call<JsonElement> getAbsencesByYear(@Header("Authorization") String auth, @Path("year") int year);

    @GET("employees/{id}/absences")
    Call<JsonElement> getEmployeeAbsences(@Header("Authorization") String auth, @Path("id") int employeeKey);

    @GET("employees/{id}/overtimes")
    Call<JsonElement> getEmployeeOvertimes(@Header("Authorization") String auth, @Path("id") int employeeKey);

    @GET("users/me")
    Call<JsonElement> getCurrentUser(@Header("Authorization") String auth);
}
