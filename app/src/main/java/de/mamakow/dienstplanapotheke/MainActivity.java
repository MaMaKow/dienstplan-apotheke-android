package de.mamakow.dienstplanapotheke;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkHandler networkHandler = new NetworkHandler(this);
        networkHandler.fetchRoster();
    }
}
