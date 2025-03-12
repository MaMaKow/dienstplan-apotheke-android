package de.mamakow.dienstplanapotheke.model;

import java.time.LocalDate;
import java.util.List;

public class Roster {
    private List<RosterDay> rosterDayList;

    public List<RosterDay> getRosterDayList() {
        return rosterDayList;
    }

    public void setRosterDayList(List<RosterDay> rosterDayList) {
        this.rosterDayList = rosterDayList;
    }

    public RosterDay getRosterOnDay(LocalDate localDate) {
        for (RosterDay rosterDay : rosterDayList) {
            if (rosterDay.getLocalDate().equals(localDate)) {
                return rosterDay;
            }
        }
        return null;
    }
}
