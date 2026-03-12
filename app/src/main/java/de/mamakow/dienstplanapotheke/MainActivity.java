package de.mamakow.dienstplanapotheke;

import android.os.Bundle;

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

    private MainViewModel viewModel;
    private RosterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewRoster);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RosterAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        LocalDate today = LocalDate.now();
        //LocalDate mondayDate = today.minusDays((today.getDayOfWeek().getValue() - 1));
        LocalDate firstMondayInJuly = LocalDate.of(today.getYear(), Month.JULY, 1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate sundayDate = firstMondayInJuly.plusDays(6);
        int employeeKey = 7;
        viewModel.getRoster(firstMondayInJuly, sundayDate).observe(this, roster -> {
            if (roster != null) {
                adapter.setRosterDays(roster.getRosterDays());
            }
        });

        viewModel.getEmployees().observe(this, employees -> {
            if (employees != null) {
                adapter.setEmployees(employees);
            }
        });

        viewModel.refreshData(firstMondayInJuly, sundayDate, employeeKey);
    }
}
