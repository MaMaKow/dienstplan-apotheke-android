package de.mamakow.dienstplanapotheke.database;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

import de.mamakow.dienstplanapotheke.model.RosterItem;

@Dao
public interface RosterItemDao {
    String TAG = "SYNC_DEBUG_ROSTER";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRosterItems(List<RosterItem> rosterItems);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRosterItem(RosterItem rosterItem);

    @Update
    void updateRosterItem(RosterItem rosterItem);

    @Delete
    void deleteRosterItem(RosterItem rosterItem);

    @Query("SELECT * FROM roster_table")
    LiveData<List<RosterItem>> getAllRosterItems();

    @Query("SELECT * FROM roster_table WHERE local_date >= :startDate AND local_date <= :endDate ORDER BY local_date ASC, duty_start_date_time ASC")
    LiveData<List<RosterItem>> getRosterItemsForDateRange(LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM roster_table WHERE local_date >= :startDate AND local_date <= :endDate AND employee_key = :employeeKey ORDER BY local_date ASC, duty_start_date_time ASC")
    LiveData<List<RosterItem>> getRosterItemsForEmployeeAndDateRange(int employeeKey, LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM roster_table WHERE employee_key = :employeeKey")
    LiveData<List<RosterItem>> getRosterItemsForEmployee(int employeeKey);

    @Query("SELECT * FROM roster_table WHERE local_date >= :startDate AND local_date <= :endDate AND branch_id = :branchId ORDER BY local_date ASC, duty_start_date_time ASC")
    LiveData<List<RosterItem>> getRosterItemsForBranchAndDateRange(int branchId, LocalDate startDate, LocalDate endDate);

    @Query("DELETE FROM roster_table WHERE employee_key = :employeeKey AND local_date >= :startDate AND local_date <= :endDate")
    int deleteForEmployeeInRange(int employeeKey, LocalDate startDate, LocalDate endDate);

    @Query("DELETE FROM roster_table WHERE branch_id = :branchId AND local_date >= :startDate AND local_date <= :endDate")
    int deleteForBranchInRange(int branchId, LocalDate startDate, LocalDate endDate);

    @Transaction
    default void syncRosterItemsForEmployee(int employeeKey, LocalDate startDate, LocalDate endDate, List<RosterItem> rosterItems) {
        // Step 1: Clean slate for the sync range
        int deleted = deleteForEmployeeInRange(employeeKey, startDate, endDate);

        // Step 2: Insert new data
        if (rosterItems != null && !rosterItems.isEmpty()) {
            insertRosterItems(rosterItems);
        }

        Log.i(TAG, "Sync Employee " + employeeKey + " (" + startDate + " to " + endDate + "): Received " + (rosterItems != null ? rosterItems.size() : 0) + " items. Removed " + deleted + " local items.");
    }

    @Transaction
    default void syncRosterItemsForBranch(int branchId, LocalDate startDate, LocalDate endDate, List<RosterItem> rosterItems) {
        int deleted = deleteForBranchInRange(branchId, startDate, endDate);
        if (rosterItems != null && !rosterItems.isEmpty()) {
            insertRosterItems(rosterItems);
        }
        Log.i(TAG, "Sync Branch " + branchId + " (" + startDate + " to " + endDate + "): Received " + (rosterItems != null ? rosterItems.size() : 0) + " items. Removed " + deleted + " local items.");
    }
}
