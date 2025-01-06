package de.mamakow.dienstplanapotheke;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.mamakow.dienstplanapotheke.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isNotLoggedIn()) {
            sessionManager.performLogin();
        } else {
            sessionManager.logout();
        }
    }
}
