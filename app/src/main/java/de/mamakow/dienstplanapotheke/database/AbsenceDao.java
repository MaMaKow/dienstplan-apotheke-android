package de.mamakow.dienstplanapotheke.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import de.mamakow.dienstplanapotheke.model.Absence;

@Dao
public interface AbsenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAbsences(List<Absence> absences);

    @Query("SELECT * FROM absence_table")
    LiveData<List<Absence>> getAllAbsencesLiveData();

    @Query("SELECT * FROM absence_table WHERE employee_key = :employeeId")
    LiveData<List<Absence>> getAbsencesByEmployeeId(int employeeId);

    @Query("SELECT * FROM absence_table WHERE employee_key = :employeeId AND (strftime('%Y', start_date) = :year OR strftime('%Y', end_date) = :year)")
    LiveData<List<Absence>> getAbsencesByEmployeeIdAndYear(int employeeId, String year);

    @Query("DELETE FROM absence_table")
    void clearAbsences();

    @Query("DELETE FROM absence_table WHERE employee_key = :employeeId")
    void deleteAbsencesByEmployeeId(int employeeId);
}
