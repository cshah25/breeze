package com.example.breeze_seas;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.annotation.ContentView;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminDashboardFragmentTest {
    // Declared to use in @Before and @After
    private FragmentScenario<AdminDashboardFragment> scenario;

    @Before
    public void setUp() {
        // This launches the indicated fragment before each @Test
        scenario = FragmentScenario.launchInContainer(AdminDashboardFragment.class);
    }

    @After
    public void tearDown() {
        // This destroys the fragment after each @Test
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void testBackArrow_isVisible() {
        // Check if it exists
        onView(withId(R.id.ad_btn_back_arrow)).check(matches(isDisplayed()));
    }
}
