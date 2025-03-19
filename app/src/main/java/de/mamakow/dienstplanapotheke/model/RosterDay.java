package de.mamakow.dienstplanapotheke.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
}
