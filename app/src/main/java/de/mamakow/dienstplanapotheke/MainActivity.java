package de.mamakow.dienstplanapotheke;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.database.EmployeeDao;
import de.mamakow.dienstplanapotheke.database.RosterItemDao;
import de.mamakow.dienstplanapotheke.model.Employee;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.repository.EmployeeRepository;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the RosterItemDao instance from the database
        RosterItemDao rosterItemDao = AppDatabase.getDatabase(this).rosterDao();
        // Perform login
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        }
        RetrofitNetworkHandler retrofitNetworkHandler = new RetrofitNetworkHandler(this);
        RosterRepository rosterRepository = new RosterRepository(retrofitNetworkHandler, rosterItemDao, sessionManager);
        LocalDate today = LocalDate.now();
        LocalDate mondayDate = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        LocalDate sundayDate = mondayDate.plusDays(6);
        LiveData<Roster> rosterData = rosterRepository.getRosterData(mondayDate, sundayDate);
        rosterData.observe(this, roster -> {
            List<RosterDay> rosterDays = roster.getRosterDays();
            for (RosterDay rosterDay : rosterDays) {
                Log.d("MainActivity", "RosterDay: " + rosterDay.getLocalDate());
                List<RosterItem> rosterItemList = rosterDay.getRosterItems();
                for (RosterItem rosterItem : rosterItemList) {
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getEmployeeKey());
                    int employeeKey = rosterItem.getEmployeeKey();
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getBranchId());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getDutyStartDateTime());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getDutyEndDateTime());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getBreakStartDateTime());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getBreakEndDateTime());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getBreakEndDateTime());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getWorkingHours());
                    Log.d("MainActivity", "  RosterItem: " + rosterItem.getComment());
                    EmployeeDao employeeDao = AppDatabase.getDatabase(this).employeeDao();
                    Employee employee = new EmployeeRepository(employeeDao).getEmployeeById(employeeKey);
                    Log.d("MainActivity", "  RosterItem: " + employee.getEmployeeFullName());
                }
            }
        });
    }
}