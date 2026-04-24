package de.mamakow.dienstplanapotheke.model;

import java.util.ArrayList;
import java.util.List;

public class AbsenceDayData {
    private final List<AbsenceWithName> absences = new ArrayList<>();
    private int count = 0;
    private int pharmacistCount = 0;
    private boolean hasNotApproved = false; // Platzhalter für Genehmigungsstatus

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPharmacistCount() {
        return pharmacistCount;
    }

    public void setPharmacistCount(int pharmacistCount) {
        this.pharmacistCount = pharmacistCount;
    }

    public List<AbsenceWithName> getAbsences() {
        return absences;
    }

    public void addAbsence(AbsenceWithName absence) {
        this.absences.add(absence);
        this.count++;
        if (absence.isPharmacist()) {
            this.pharmacistCount++;
        }
    }

    public boolean isHasNotApproved() {
        return hasNotApproved;
    }

    public void setHasNotApproved(boolean hasNotApproved) {
        this.hasNotApproved = hasNotApproved;
    }
}
