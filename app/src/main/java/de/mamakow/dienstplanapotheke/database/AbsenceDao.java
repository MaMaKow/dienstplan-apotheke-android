package de.mamakow.dienstplanapotheke.database;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.model.Absence;

@Dao
public interface AbsenceDao {

    String TAG = "SYNC_DEBUG_ABSENCE";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAbsences(List<Absence> absences);

    @Query("SELECT * FROM absence_table")
    LiveData<List<Absence>> getAllAbsencesLiveData();

    @Query("SELECT * FROM absence_table WHERE strftime('%Y', start_date) = :year OR strftime('%Y', end_date) = :year")
    LiveData<List<Absence>> getAllAbsencesByYearLiveData(String year);

    @Query("SELECT * FROM absence_table WHERE employee_key = :employeeId")
    LiveData<List<Absence>> getAbsencesByEmployeeId(int employeeId);

    @Query("SELECT * FROM absence_table WHERE employee_key = :employeeId AND (strftime('%Y', start_date) = :year OR strftime('%Y', end_date) = :year)")
    LiveData<List<Absence>> getAbsencesByEmployeeIdAndYear(int employeeId, String year);

    @Query("DELETE FROM absence_table")
    void clearAbsences();

    @Query("DELETE FROM absence_table WHERE employee_key = :employeeId")
    void deleteAbsencesByEmployeeId(int employeeId);

    @Query("DELETE FROM absence_table WHERE employee_key = :employeeId AND (strftime('%Y', start_date) = :year OR strftime('%Y', end_date) = :year) AND id NOT IN (:receivedIds)")
    int deleteRemovedAbsencesForEmployeeInYear(int employeeId, String year, List<Integer> receivedIds);

    @Transaction
    default void syncAbsencesForEmployee(int employeeKey, int year, List<Absence> absences) {
        List<Integer> receivedIds = new ArrayList<>();
        if (absences != null) {
            for (Absence absence : absences) {
                receivedIds.add(absence.getId());
            }
        }
        int deletedCount = deleteRemovedAbsencesForEmployeeInYear(employeeKey, String.valueOf(year), receivedIds);
        if (absences != null && !absences.isEmpty()) {
            insertAbsences(absences);
        }
        Log.i(TAG, "Sync Absences Employee " + employeeKey + " for year " + year + ": Received " + (absences != null ? absences.size() : 0) + " items. Deleted " + deletedCount + " obsolete items.");
    }
}
