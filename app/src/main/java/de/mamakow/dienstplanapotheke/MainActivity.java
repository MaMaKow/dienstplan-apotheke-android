package de.mamakow.dienstplanapotheke;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.view.AbsenceFragment;
import de.mamakow.dienstplanapotheke.view.OvertimeFragment;
import de.mamakow.dienstplanapotheke.view.RosterBranchFragment;
import de.mamakow.dienstplanapotheke.view.RosterEmployeeFragment;

/**
 * MainActivity handles Session Management and Fragment Navigation.
 * All UI logic for rosters, absences, and overtime is delegated to specialized Fragments.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isBaseUrlSet()) {
            showUrlInputDialog(sessionManager);
        } else {
            checkLoginAndProceed(sessionManager);
        }
    }

    private void checkLoginAndProceed(SessionManager sessionManager) {
        if (sessionManager.isNotLoggedIn()) {
            showLoginDialog(sessionManager);
        } else {
            if (!sessionManager.hasUserData()) {
                sessionManager.refreshSessionUserData(new LoginCallback() {
                    @Override
                    public void onSuccess(String token) {
                        runOnUiThread(() -> proceedWithInitialization());
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.e(TAG, "Error refreshing user data: " + exception.getLocalizedMessage());
                        runOnUiThread(() -> showLoginDialog(sessionManager));
                    }
                });
            } else {
                proceedWithInitialization();
            }
        }
    }

    private void showLoginDialog(SessionManager sessionManager) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null);
        EditText editTextUsername = dialogView.findViewById(R.id.editTextUsername);
        EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);

        // Benutzername im Voraus ausfüllen, wenn bekannt
        String savedUsername = sessionManager.getStoredUsername();
        if (!savedUsername.isEmpty()) {
            editTextUsername.setText(savedUsername);
            // Optional: Fokus direkt auf das Passwort-Feld setzen,
            // da der Name ja schon da ist
            editTextPassword.requestFocus();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.login_title)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton(R.string.login_button, (dialog, which) -> {
                    String username = editTextUsername.getText().toString().trim();
                    String password = editTextPassword.getText().toString().trim();

                    if (!username.isEmpty() && !password.isEmpty()) {
                        sessionManager.performLogin(username, password, new LoginCallback() {
                            @Override
                            public void onSuccess(String token) {
                                runOnUiThread(() -> proceedWithInitialization());
                            }

                            @Override
                            public void onFailure(Exception exception) {
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                                    showLoginDialog(sessionManager);
                                });
                            }
                        });
                    } else {
                        showLoginDialog(sessionManager);
                    }
                })
                .show();
    }

    private void proceedWithInitialization() {

        setupNavigation();

        // Initial fragment load
        if (getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment) == null) {
            switchFragment(new RosterEmployeeFragment());
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_roster_employee) {
                switchFragment(new RosterEmployeeFragment());
            } else if (itemId == R.id.nav_roster_branch) {
                switchFragment(new RosterBranchFragment());
            } else if (itemId == R.id.nav_absences) {
                switchFragment(new AbsenceFragment());
            } else if (itemId == R.id.nav_overtime) {
                switchFragment(new OvertimeFragment());
            }
            return true;
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                SessionManager sessionManager = new SessionManager(this);
                sessionManager.logout();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void showUrlInputDialog(SessionManager sessionManager) {
        final EditText input = new EditText(this);
        input.setHint(getString(R.string.test_page_url));
        input.setText(getString(R.string.test_page_url));

        new AlertDialog.Builder(this)
                .setTitle("API URL Configuration")
                .setMessage("Please enter the base URL for your roster:")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        sessionManager.saveBaseUrl(url);
                        checkLoginAndProceed(sessionManager);
                    } else {
                        showUrlInputDialog(sessionManager);
                    }
                })
                .show();
    }
}
