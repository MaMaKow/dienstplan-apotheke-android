package de.mamakow.dienstplanapotheke.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Roster {
    private List<RosterDay> rosterDays;

    public Roster() {
        this.rosterDays = new ArrayList<>();
    }

    public List<RosterDay> getRosterDays() {
        return rosterDays;
    }

    public void setRosterDays(List<RosterDay> rosterDays) {
        this.rosterDays = rosterDays;
    }

    public void addRosterDay(RosterDay rosterDay) {
        rosterDays.add(rosterDay);
    }

    public RosterDay getRosterOnDay(LocalDate localDate) {
        for (RosterDay rosterDay : rosterDays) {
            if (rosterDay.getLocalDate().equals(localDate)) {
                return rosterDay;
            }
        }
        return null;
    }
}
