package de.mamakow.dienstplanapotheke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;

public class RosterTest {

    @Test
    public void testAddAndGetRosterDay() {
        Roster roster = new Roster();
        LocalDate today = LocalDate.now();
        RosterDay rosterDay = new RosterDay(today);

        roster.addRosterDay(rosterDay);

        assertEquals(1, roster.getRosterDays().size());
        assertEquals(rosterDay, roster.getRosterOnDay(today));
    }

    @Test
    public void testGetRosterOnNonExistentDayReturnsNull() {
        Roster roster = new Roster();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        roster.addRosterDay(new RosterDay(today));

        assertNull(roster.getRosterOnDay(tomorrow));
    }

    @Test
    public void testRosterDayContainsItems() {
        LocalDate today = LocalDate.now();
        RosterDay rosterDay = new RosterDay(today);
        RosterItem item = new RosterItem();
        item.setComment("Frühschicht");

        rosterDay.addRosterItem(item);

        assertEquals(1, rosterDay.getRosterItems().size());
        assertEquals("Frühschicht", rosterDay.getRosterItems().get(0).getComment());
    }

    @Test
    public void testCalculateNetWorkingHours_WithPause() {
        // Arrange: 08:00 - 16:30 mit 30 Min Pause
        RosterItem item = new RosterItem();
        item.setDutyStartDateTime(LocalDateTime.of(2023, 10, 27, 8, 0));
        item.setDutyEndDateTime(LocalDateTime.of(2023, 10, 27, 16, 30));
        item.setBreakStartDateTime(LocalDateTime.of(2023, 10, 27, 12, 0));
        item.setBreakEndDateTime(LocalDateTime.of(2023, 10, 27, 12, 30));

        // Act
        double hours = item.calculateNetWorkingHours();

        // Assert: 8.5 Stunden Gesamt - 0.5 Stunden Pause = 8.0 Stunden
        assertEquals(8.0, hours, 0.01);
    }

    @Test
    public void testCalculateNetWorkingHours_WithoutPause() {
        // Arrange: 08:00 - 12:00 ohne Pause
        RosterItem item = new RosterItem();
        item.setDutyStartDateTime(LocalDateTime.of(2023, 10, 27, 8, 0));
        item.setDutyEndDateTime(LocalDateTime.of(2023, 10, 27, 12, 0));

        // Act
        double hours = item.calculateNetWorkingHours();

        // Assert
        assertEquals(4.0, hours, 0.01);
    }

    @Test
    public void testCalculateNetWorkingHours_NullValues() {
        RosterItem item = new RosterItem();
        assertEquals(0.0, item.calculateNetWorkingHours(), 0.01);

        item.setDutyStartDateTime(LocalDateTime.now());
        assertEquals(0.0, item.calculateNetWorkingHours(), 0.01);
    }
}
