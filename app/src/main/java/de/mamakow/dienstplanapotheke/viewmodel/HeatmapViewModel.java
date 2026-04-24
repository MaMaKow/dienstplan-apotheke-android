package de.mamakow.dienstplanapotheke.viewmodel;

import android.app.Application;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.model.Absence;
import de.mamakow.dienstplanapotheke.model.AbsenceDayData;
import de.mamakow.dienstplanapotheke.model.AbsenceMonthData;
import de.mamakow.dienstplanapotheke.model.AbsenceWithName;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.repository.AbsenceRepository;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;

public class HeatmapViewModel extends AndroidViewModel {

    private final AbsenceRepository absenceRepository;
    private final EmployeeRepository employeeRepository;
    private final MediatorLiveData<List<AbsenceMonthData>> heatmapData = new MediatorLiveData<>();

    public HeatmapViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        // Annahme: Repositories werden hier einfach instanziiert, im echten Projekt evtl. über DI
        absenceRepository = new AbsenceRepository(db.absenceDao(), null, null);
        employeeRepository = new EmployeeRepository(db.employeeDao(), null, null);

        LiveData<List<Absence>> absencesLiveData = absenceRepository.getAllAbsencesByYearLiveData(LocalDate.now().getYear());
        LiveData<List<Employee>> employeesLiveData = employeeRepository.getAllEmployeesLiveData();

        heatmapData.addSource(absencesLiveData, absences -> combine(absences, employeesLiveData.getValue()));
        heatmapData.addSource(employeesLiveData, employees -> combine(absencesLiveData.getValue(), employees));
    }

    public LiveData<List<AbsenceMonthData>> getHeatmapData() {
        return heatmapData;
    }

    private void combine(List<Absence> absences, List<Employee> employees) {
        if (absences == null || employees == null) return;

        Map<Integer, Employee> employeeMap = new HashMap<>();
        for (Employee e : employees) {
            employeeMap.put(e.getEmployeeKey(), e);
        }

        LongSparseArray<AbsenceDayData> dayDataMap = new LongSparseArray<>();

        for (Absence absence : absences) {
            Employee emp = employeeMap.get(absence.getEmployeeKey());
            String name = emp != null ? emp.getEmployeeFullName() : "Unbekannt";
            String profession = emp != null ? emp.getEmployeeProfession() : "";
            AbsenceWithName awn = new AbsenceWithName(absence, name, profession);

            LocalDate start = absence.getStartDate();
            LocalDate end = absence.getEndDate();

            if (start == null || end == null) continue;

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                long epochDay = date.toEpochDay();
                AbsenceDayData data = dayDataMap.get(epochDay);
                if (data == null) {
                    data = new AbsenceDayData();
                    dayDataMap.put(epochDay, data);
                }
                data.addAbsence(awn);
            }
        }

        List<AbsenceMonthData> months = new ArrayList<>();
        int year = LocalDate.now().getYear();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            List<LocalDate> days = new ArrayList<>();
            Map<Long, AbsenceDayData> monthDayData = new HashMap<>();

            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                LocalDate date = ym.atDay(d);
                days.add(date);
                AbsenceDayData data = dayDataMap.get(date.toEpochDay());
                if (data != null) {
                    monthDayData.put(date.toEpochDay(), data);
                }
            }
            months.add(new AbsenceMonthData(ym, days, monthDayData));
        }

        heatmapData.setValue(months);
    }
}
