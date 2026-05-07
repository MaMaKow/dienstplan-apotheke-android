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
import java.time.LocalDateTime;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.database.RosterItemDao;
import de.mamakow.dienstplanapotheke.model.RosterItem;

@RunWith(AndroidJUnit4.class)
public class RosterDatabaseTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private RosterItemDao rosterItemDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Wir nutzen eine In-Memory-Datenbank für Tests, damit sie nach dem Test gelöscht wird
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        rosterItemDao = db.rosterDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void writeRosterItemAndReadInList() throws Exception {
        // Arrange
        RosterItem item = new RosterItem();
        item.setEmployeeKey(1);
        item.setLocalDate(LocalDate.now());
        item.setDutyStartDateTime(LocalDateTime.now());
        item.setDutyEndDateTime(LocalDateTime.now().plusHours(8));
        item.setComment("Test Dienst");
        item.setStatus(RosterItem.Status.CONFIRMED);

        // Act
        rosterItemDao.insertRosterItem(item);

        // Assert
        List<RosterItem> items = LiveDataTestUtil.getOrAwaitValue(rosterItemDao.getAllRosterItems());
        assertEquals(1, items.size());
        assertEquals("Test Dienst", items.get(0).getComment());
        assertEquals(RosterItem.Status.CONFIRMED, items.get(0).getStatus());
    }

    @Test
    public void testGetRosterItemsForDateRange() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();

        RosterItem item1 = new RosterItem();
        item1.setLocalDate(today);
        item1.setEmployeeKey(1);

        RosterItem item2 = new RosterItem();
        item2.setLocalDate(today.plusDays(5));
        item2.setEmployeeKey(2);

        rosterItemDao.insertRosterItem(item1);
        rosterItemDao.insertRosterItem(item2);

        // Act
        // Suche nur für heute bis in 2 Tagen -> sollte nur item1 finden
        List<RosterItem> items = LiveDataTestUtil.getOrAwaitValue(
                rosterItemDao.getRosterItemsForDateRange(today, today.plusDays(2))
        );

        // Assert
        assertEquals(1, items.size());
        assertEquals(1, items.get(0).getEmployeeKey());
    }

    @Test
    public void updateRosterItem() throws Exception {
        // Arrange
        RosterItem item = new RosterItem();
        item.setEmployeeKey(1);
        item.setLocalDate(LocalDate.now());
        item.setDutyStartDateTime(LocalDateTime.now());
        item.setDutyEndDateTime(LocalDateTime.now().plusHours(8));
        item.setStatus(RosterItem.Status.TENTATIVE);
        rosterItemDao.insertRosterItem(item);

        // Act
        List<RosterItem> items = LiveDataTestUtil.getOrAwaitValue(rosterItemDao.getAllRosterItems());
        RosterItem itemToUpdate = items.get(0);
        itemToUpdate.setStatus(RosterItem.Status.CONFIRMED);
        LocalDateTime newEndTime = itemToUpdate.getDutyEndDateTime().plusHours(1);
        itemToUpdate.setDutyEndDateTime(newEndTime);

        rosterItemDao.updateRosterItem(itemToUpdate);

        // Assert
        List<RosterItem> updatedItems = LiveDataTestUtil.getOrAwaitValue(rosterItemDao.getAllRosterItems());
        assertEquals(1, updatedItems.size());
        assertEquals(RosterItem.Status.CONFIRMED, updatedItems.get(0).getStatus());
        assertEquals(newEndTime, updatedItems.get(0).getDutyEndDateTime());
    }

    @Test
    public void deleteRosterItem() throws Exception {
        // Arrange
        RosterItem item = new RosterItem();
        item.setEmployeeKey(1);
        item.setLocalDate(LocalDate.now());
        rosterItemDao.insertRosterItem(item);

        // Act
        List<RosterItem> itemsBeforeDelete = LiveDataTestUtil.getOrAwaitValue(rosterItemDao.getAllRosterItems());
        assertEquals(1, itemsBeforeDelete.size());
        rosterItemDao.deleteRosterItem(itemsBeforeDelete.get(0));

        // Assert
        List<RosterItem> itemsAfterDelete = LiveDataTestUtil.getOrAwaitValue(rosterItemDao.getAllRosterItems());
        assertTrue(itemsAfterDelete.isEmpty());
    }

    @Test
    public void testEmptyDatabase() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();

        // Act
        List<RosterItem> items = LiveDataTestUtil.getOrAwaitValue(
                rosterItemDao.getRosterItemsForDateRange(today, today.plusDays(1))
        );

        // Assert
        assertTrue("Die Liste sollte leer sein, nicht null", items != null && items.isEmpty());
    }

    @Test
    public void testGetRosterItemsForEmployee() throws Exception {
        // Arrange
        int employeeId1 = 101;
        int employeeId2 = 102;

        RosterItem item1 = new RosterItem();
        item1.setEmployeeKey(employeeId1);
        item1.setLocalDate(LocalDate.now());

        RosterItem item2 = new RosterItem();
        item2.setEmployeeKey(employeeId2);
        item2.setLocalDate(LocalDate.now());

        rosterItemDao.insertRosterItem(item1);
        rosterItemDao.insertRosterItem(item2);

        // Act
        List<RosterItem> results = LiveDataTestUtil.getOrAwaitValue(
                rosterItemDao.getRosterItemsForEmployee(employeeId1)
        );

        // Assert
        assertEquals(1, results.size());
        assertEquals(employeeId1, results.get(0).getEmployeeKey());
    }
}
