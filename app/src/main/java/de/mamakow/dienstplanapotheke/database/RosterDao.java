package de.mamakow.dienstplanapotheke.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.time.LocalDate;
import java.util.List;

import de.mamakow.dienstplanapotheke.model.Roster;

@Dao
public interface RosterDao {

    @Query("SELECT * FROM Roster")
    LiveData<List<Roster>> getAllRosters();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRoster(Roster roster);

    @Delete
    void deleteRoster(Roster roster);

    @Query("DELETE FROM Roster")
    void clearRosters();

    @Query("SELECT * FROM Roster WHERE date >= :startDate AND date <= :endDate")
    LiveData<List<Roster>> getRoster(LocalDate startDate, LocalDate endDate);
}