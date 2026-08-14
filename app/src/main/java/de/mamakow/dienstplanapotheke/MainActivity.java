package de.mamakow.dienstplanapotheke;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import de.mamakow.dienstplanapotheke.network.LoginCallback;
import de.mamakow.dienstplanapotheke.session.SessionManager;
import de.mamakow.dienstplanapotheke.util.SystemUtils;
import de.mamakow.dienstplanapotheke.util.UIError;
import de.mamakow.dienstplanapotheke.view.AbsenceFragment;
import de.mamakow.dienstplanapotheke.view.OvertimeFragment;
import de.mamakow.dienstplanapotheke.view.RosterBranchFragment;
import de.mamakow.dienstplanapotheke.view.RosterEmployeeFragment;
import de.mamakow.dienstplanapotheke.view.SettingsFragment;
import de.mamakow.dienstplanapotheke.viewModel.MainViewModel;

/**
 * MainActivity handles Session Management and Fragment Navigation.
 * All UI logic for rosters, absences, and overtime is delegated to specialized Fragments.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate and setContentView
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themeValue = prefs.getString("pref_ui_theme_mode", "system");
        SystemUtils.applyTheme(themeValue);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        observeErrors();

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isBaseUrlSet()) {
            showUrlInputDialog(sessionManager);
        } else {
            checkLoginAndProceed(sessionManager);
        }
    }

    private void observeErrors() {
        viewModel.getUiError().observe(this, event -> {
            UIError error = event.getContentIfNotHandled();
            if (error != null) {
                showError(error);
            }
        });
    }

    private void showError(UIError error) {
        View contextView = findViewById(android.R.id.content);
        if (error.getType() == UIError.Type.TOAST) {
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Snackbar snackbar = Snackbar.make(contextView, error.getMessage(), Snackbar.LENGTH_LONG);
            if (error.getType() == UIError.Type.SNACKBAR_WITH_RETRY && error.getRetryAction() != null) {
                snackbar.setAction("Wiederholen", v -> error.getRetryAction().run());
            }
            snackbar.show();
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
            switchFragment(new RosterEmployeeFragment(), false);
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_roster_employee) {
                switchFragment(new RosterEmployeeFragment(), false);
            } else if (itemId == R.id.nav_roster_branch) {
                switchFragment(new RosterBranchFragment(), false);
            } else if (itemId == R.id.nav_absences) {
                switchFragment(new AbsenceFragment(), false);
            } else if (itemId == R.id.nav_overtime) {
                switchFragment(new OvertimeFragment(), false);
            }
            return true;
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                switchFragment(new SettingsFragment(), true);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.einstellungen);
                }
            } else if (id == R.id.nav_logout) {
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

    private void switchFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
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
