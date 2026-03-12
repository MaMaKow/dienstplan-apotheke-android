package de.mamakow.dienstplanapotheke;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.view.RosterAdapter;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainViewModel viewModel;
    private RosterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getSessionToken();
        Log.i(TAG, "Session Token vorhanden: " + (token != null && !token.isEmpty()));

        if (sessionManager.isNotLoggedIn()) {
            Log.i(TAG, "Nicht eingeloggt, starte Login...");
            sessionManager.performLogin();
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewRoster);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RosterAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        LocalDate today = LocalDate.now();
        LocalDate firstMondayInJuly = LocalDate.of(today.getYear(), Month.JULY, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate sundayDate = firstMondayInJuly.plusDays(6);
        int employeeKey = 7;

        viewModel.getRoster(firstMondayInJuly, sundayDate).observe(this, roster -> {
            if (roster != null) {
                Log.i(TAG, "Roster Daten empfangen: " + roster.getRosterDays().size() + " Tage");
                adapter.setRosterDays(roster.getRosterDays());
            }
        });

        viewModel.getEmployees().observe(this, employees -> {
            if (employees != null) {
                Log.i(TAG, "Mitarbeiter aus DB: " + employees.size());
                adapter.setEmployees(employees);
            }
        });

        viewModel.getBranches().observe(this, branches -> {
            if (branches != null) {
                Log.i(TAG, "Filialen aus DB: " + branches.size());
                adapter.setBranches(branches);
            }
        });

        Log.i(TAG, "Starte Daten-Refresh...");
        viewModel.refreshData(firstMondayInJuly, sundayDate, employeeKey);
    }
}
