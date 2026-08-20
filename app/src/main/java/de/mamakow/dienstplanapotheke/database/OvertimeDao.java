package de.mamakow.dienstplanapotheke.database;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import de.mamakow.dienstplanapotheke.model.Overtime;

@Dao
public interface OvertimeDao {

    String TAG = "SYNC_DEBUG_OVERTIME";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOvertimes(List<Overtime> overtimes);

    @Query("SELECT * FROM overtime_table")
    LiveData<List<Overtime>> getAllOvertimesLiveData();

    @Query("SELECT * FROM overtime_table WHERE date LIKE :year || '-%' ORDER BY date DESC")
    LiveData<List<Overtime>> getAllOvertimeByYearLiveData(String year);

    @Query("SELECT * FROM overtime_table WHERE employee_key = :employeeId")
    LiveData<List<Overtime>> getOvertimesByEmployeeId(int employeeId);

    @Query("SELECT * FROM overtime_table WHERE employee_key = :employeeId AND date LIKE :year || '-%' ORDER BY date DESC")
    LiveData<List<Overtime>> getOvertimesByEmployeeIdAndYear(int employeeId, String year);

    @Query("DELETE FROM overtime_table")
    void clearOvertimes();

    @Query("DELETE FROM overtime_table WHERE employee_key = :employeeId")
    void deleteOvertimesByEmployeeId(int employeeId);

    @Transaction
    default void syncOvertimesForEmployee(int employeeKey, List<Overtime> overtimes) {
        // Step 1: Remove existing data for this employee to ensure we don't have stale entries
        deleteOvertimesByEmployeeId(employeeKey);

        // Step 2: Insert the fresh data from the API
        if (overtimes != null && !overtimes.isEmpty()) {
            // Set the employeeKey for each item just in case the API doesn't provide it in the object
            for (Overtime ot : overtimes) {
                ot.setEmployeeKey(employeeKey);
            }
            insertOvertimes(overtimes);
        }

        Log.i(TAG, "Sync Overtime Employee " + employeeKey + ": Received " + (overtimes != null ? overtimes.size() : 0) + " items.");
    }
}
