package de.mamakow.dienstplanapotheke.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.mamakow.dienstplanapotheke.model.Overtime;

@Dao
public interface OvertimeDao {

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
}
