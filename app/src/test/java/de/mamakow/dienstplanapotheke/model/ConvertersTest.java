package de.mamakow.dienstplanapotheke.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class ConvertersTest {

    private Converters converters;

    @Before
    public void setUp() {
        converters = new Converters();
    }

    @Test
    public void testLocalDateConversion() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 10, 27);
        String expectedString = "2023-10-27";

        // Act & Assert
        String convertedString = converters.fromLocalDate(date);
        assertEquals(expectedString, convertedString);

        LocalDate convertedDate = converters.toLocalDate(expectedString);
        assertEquals(date, convertedDate);
    }

    @Test
    public void testLocalDateNullHandling() {
        assertNull(converters.fromLocalDate(null));
        assertNull(converters.toLocalDate(null));
    }

    @Test
    public void testLocalDateTimeConversion() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 27, 14, 30, 0);
        String expectedString = "2023-10-27T14:30:00";

        // Act & Assert
        String convertedString = converters.fromLocalDateTime(dateTime);
        assertEquals(expectedString, convertedString);

        LocalDateTime convertedDateTime = converters.toLocalDateTime(expectedString);
        assertEquals(dateTime, convertedDateTime);
    }

    @Test
    public void testLocalDateTimeNullHandling() {
        assertNull(converters.fromLocalDateTime(null));
        assertNull(converters.toLocalDateTime(null));
    }

    @Test
    public void testStatusConversion() {
        // Arrange
        RosterItem.Status status = RosterItem.Status.CONFIRMED;
        String expectedString = "CONFIRMED";

        // Act & Assert
        String convertedString = converters.fromStatus(status);
        assertEquals(expectedString, convertedString);

        RosterItem.Status convertedStatus = converters.toStatus(expectedString);
        assertEquals(status, convertedStatus);
    }

    @Test
    public void testStatusNullHandling() {
        assertNull(converters.fromStatus(null));
        assertNull(converters.toStatus(null));
    }

    @Test
    public void testOpeningTimesMapConversion() {
        // Arrange
        HashMap<Integer, OpeningHours> map = new HashMap<>();
        OpeningHours hours = new OpeningHours();
        hours.setStart("08:00");
        hours.setEnd("18:30");
        map.put(1, hours);

        // Act
        String json = Converters.fromOpeningTimesMap(map);
        HashMap<Integer, OpeningHours> convertedMap = Converters.toOpeningTimesMap(json);

        // Assert
        assertNotNull(convertedMap);
        assertEquals(1, convertedMap.size());
        assertEquals("08:00", convertedMap.get(1).getStart());
        assertEquals("18:30", convertedMap.get(1).getEnd());
    }

    @Test
    public void testOpeningTimesMapNullHandling() {
        // GSON typically converts null to the string "null" or returns null depending on implementation
        // The current Converters implementation doesn't check for null in map conversion
        assertNull(Converters.toOpeningTimesMap(null));
        assertEquals("null", Converters.fromOpeningTimesMap(null));
    }
}
