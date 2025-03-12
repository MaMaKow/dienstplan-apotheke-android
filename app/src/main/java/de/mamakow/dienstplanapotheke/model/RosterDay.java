package de.mamakow.dienstplanapotheke.model;

import java.time.LocalDate;
import java.util.List;

public class RosterDay {
    private LocalDate localDate;
    private List<RosterItem> rosterItemList;

    public LocalDate getLocalDate() {
        return localDate;
    }

    public List<RosterItem> getRosterItemList() {
        return rosterItemList;
    }
}
