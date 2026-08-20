package de.mamakow.dienstplanapotheke.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RosterDay {
    private final LocalDate localDate;
    private final List<RosterItem> rosterItems;

    public RosterDay(LocalDate localDate) {
        this.localDate = localDate;
        this.rosterItems = new ArrayList<>();
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public List<RosterItem> getRosterItems() {
        return rosterItems;
    }

    public void addRosterItem(RosterItem rosterItem) {
        rosterItems.add(rosterItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RosterDay rosterDay = (RosterDay) o;
        return Objects.equals(localDate, rosterDay.localDate) &&
                Objects.equals(rosterItems, rosterDay.rosterItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDate, rosterItems);
    }
}
