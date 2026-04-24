package de.mamakow.dienstplanapotheke.model;

public class AbsenceWithName {
    private final Absence absence;
    private final String employeeName;
    private final String profession;
    private final boolean isPharmacist;

    public AbsenceWithName(Absence absence, String employeeName, String profession) {
        this.absence = absence;
        this.employeeName = employeeName;
        this.profession = profession;
        // Apotheker-Check (Annahme: Profession-String enthält 'Apotheker')
        this.isPharmacist = profession != null && profession.toLowerCase().contains("apotheker");
    }

    public Absence getAbsence() {
        return absence;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getProfession() {
        return profession;
    }

    public boolean isPharmacist() {
        return isPharmacist;
    }

}
