package de.mamakow.dienstplanapotheke;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.RosterDao;
import de.mamakow.dienstplanapotheke.database.RosterDatabase;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the RosterDao instance from the database
        RosterDao rosterDao = RosterDatabase.getDatabase(this).rosterDao();
        // Perform login
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        }
        RetrofitNetworkHandler retrofitNetworkHandler = new RetrofitNetworkHandler(this);
        RosterRepository rosterRepository = new RosterRepository(retrofitNetworkHandler, rosterDao, sessionManager);
        LocalDate today = LocalDate.now();
        LocalDate mondayDate = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        LocalDate sundayDate = mondayDate.plusDays(6);
        LiveData<List<Roster>> rosterData = rosterRepository.getRosterData(mondayDate, sundayDate);
        rosterData.observe(this, roster -> {
            roster
        });
    }
}