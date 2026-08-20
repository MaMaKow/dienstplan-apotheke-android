package de.mamakow.dienstplanapotheke;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testActivityLaunch() {
        // Just verify the activity starts. 
        // Depending on login state, it either shows the main UI or a login dialog.
        // We check for the general content view.
        onView(withId(android.R.id.content)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationExists() {
        // The BottomNavigationView should be present in the layout
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }
}
