package de.mamakow.dienstplanapotheke.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDate;
import java.util.List;

import de.mamakow.dienstplanapotheke.model.RosterItem;

@Dao
public interface RosterItemDao {

    @Query("SELECT * FROM roster_table")
    LiveData<List<RosterItem>> getAllRosterItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRosterItem(RosterItem rosterItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRosterItems(List<RosterItem> rosterItems);

    @Update
    void updateRosterItem(RosterItem rosterItem);

    @Delete
    void deleteRosterItem(RosterItem rosterItem);

    @Query("DELETE FROM roster_table")
    void clearRosterItems();


    // In RosterItemDao.java
    @Query("SELECT * FROM roster_table WHERE local_date >= :startDate AND local_date <= :endDate ORDER BY local_date ASC, duty_start_date_time ASC")
    LiveData<List<RosterItem>> getRosterItemsForDateRange(LocalDate startDate, LocalDate endDate);

    @Query("SELECT * FROM roster_table WHERE local_date = :date")
    LiveData<List<RosterItem>> getRosterItemsForDate(LocalDate date);

    @Query("SELECT * FROM roster_table WHERE employee_key = :employeeKey")
    LiveData<List<RosterItem>> getRosterItemsForEmployee(int employeeKey);
}
