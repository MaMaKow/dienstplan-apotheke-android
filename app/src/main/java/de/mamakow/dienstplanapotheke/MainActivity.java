package de.mamakow.dienstplanapotheke;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import de.mamakow.dienstplanapotheke.database.AppDatabase;
import de.mamakow.dienstplanapotheke.database.RosterItemDao;
import de.mamakow.dienstplanapotheke.model.Roster;
import de.mamakow.dienstplanapotheke.model.RosterDay;
import de.mamakow.dienstplanapotheke.model.RosterItem;
import de.mamakow.dienstplanapotheke.network.RetrofitNetworkHandler;
import de.mamakow.dienstplanapotheke.repository.RosterRepository;
import de.mamakow.dienstplanapotheke.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RosterItemDao rosterItemDao = AppDatabase.getDatabase(this).rosterDao();
        SessionManager sessionManager = new SessionManager(this);

        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        }

        RetrofitNetworkHandler retrofitNetworkHandler = new RetrofitNetworkHandler(this);
        RosterRepository rosterRepository = new RosterRepository(retrofitNetworkHandler, rosterItemDao, sessionManager);

        LocalDate today = LocalDate.now();
        // Berechne Montag der aktuellen Woche
        LocalDate mondayDate = today.minusDays((today.getDayOfWeek().getValue() - 1));
        LocalDate sundayDate = mondayDate.plusDays(6);

        Log.d("MainActivity", "Abfragezeitraum: " + mondayDate + " bis " + sundayDate);

        LiveData<Roster> rosterData = rosterRepository.getRosterData(mondayDate, sundayDate);
        rosterData.observe(this, roster -> {
            List<RosterDay> rosterDays = roster.getRosterDays();
            Log.d("MainActivity", "Anzahl der gefundenen RosterTage in der DB für diesen Zeitraum: " + rosterDays.size());

            for (RosterDay rosterDay : rosterDays) {
                Log.d("MainActivity", "Tag gefunden: " + rosterDay.getLocalDate());
                for (RosterItem rosterItem : rosterDay.getRosterItems()) {
                    Log.d("MainActivity", "  Eintrag: Mitarbeiter " + rosterItem.getEmployeeKey() + " am " + rosterItem.getLocalDate());
                }
            }
        });

        // Daten vom Server laden (für Mitarbeiter 7)
        rosterRepository.fetchAndSaveRosterData(mondayDate.format(DateTimeFormatter.ISO_DATE), sundayDate.format(DateTimeFormatter.ISO_DATE), 7);
    }
}
