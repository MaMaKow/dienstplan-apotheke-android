package de.mamakow.dienstplanapotheke;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;

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
}
