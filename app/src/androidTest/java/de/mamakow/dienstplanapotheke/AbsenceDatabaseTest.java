package de.mamakow.dienstplanapotheke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AbsenceDao;
import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.model.Absence;

@RunWith(AndroidJUnit4.class)
public class AbsenceDatabaseTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private AbsenceDao absenceDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        absenceDao = db.absenceDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetAbsences() throws Exception {
        Absence absence = new Absence();
        absence.setId(1);
        absence.setEmployeeKey(101);
        absence.setStartDate(LocalDate.of(2024, 6, 1));
        absence.setEndDate(LocalDate.of(2024, 6, 10));

        List<Absence> list = new ArrayList<>();
        list.add(absence);
        absenceDao.insertAbsences(list);

        List<Absence> result = LiveDataTestUtil.getOrAwaitValue(absenceDao.getAbsencesByEmployeeId(101));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    public void deleteAbsencesByEmployeeId() throws Exception {
        Absence a1 = new Absence();
        a1.setId(1);
        a1.setEmployeeKey(101);
        Absence a2 = new Absence();
        a2.setId(2);
        a2.setEmployeeKey(102);

        List<Absence> list = new ArrayList<>();
        list.add(a1);
        list.add(a2);
        absenceDao.insertAbsences(list);

        absenceDao.deleteAbsencesByEmployeeId(101);

        List<Absence> result1 = LiveDataTestUtil.getOrAwaitValue(absenceDao.getAbsencesByEmployeeId(101));
        List<Absence> result2 = LiveDataTestUtil.getOrAwaitValue(absenceDao.getAbsencesByEmployeeId(102));

        assertTrue(result1.isEmpty());
        assertEquals(1, result2.size());
    }

    @Test
    public void syncAbsencesForEmployee_DeletesObsolete() throws Exception {
        // Arrange: 2 existing absences for employee 101 in 2024
        Absence a1 = new Absence();
        a1.setId(10);
        a1.setEmployeeKey(101);
        a1.setStartDate(LocalDate.of(2024, 1, 1));
        a1.setEndDate(LocalDate.of(2024, 1, 5));

        Absence a2 = new Absence();
        a2.setId(11);
        a2.setEmployeeKey(101);
        a2.setStartDate(LocalDate.of(2024, 2, 1));
        a2.setEndDate(LocalDate.of(2024, 2, 5));

        List<Absence> initial = new ArrayList<>();
        initial.add(a1);
        initial.add(a2);
        absenceDao.insertAbsences(initial);

        // Act: Sync with only a1 (a2 is missing)
        List<Absence> synced = new ArrayList<>();
        synced.add(a1);
        absenceDao.syncAbsencesForEmployee(101, 2024, synced);

        // Assert: a2 should be gone
        List<Absence> result = LiveDataTestUtil.getOrAwaitValue(absenceDao.getAbsencesByEmployeeIdAndYear(101, "2024"));
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getId());
    }
}
