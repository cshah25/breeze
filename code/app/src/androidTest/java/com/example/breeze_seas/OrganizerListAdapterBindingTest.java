package com.example.breeze_seas;

import static org.junit.Assert.assertEquals;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Binding tests for {@link OrganizerListAdapter}.
 *
 * <p>These tests verify the organizer-side list row fallbacks for entrant names, secondary
 * contact text, and trailing status chips.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerListAdapterBindingTest {

    /**
     * Verifies that organizer rows prefer the entrant's full name and combine email and phone
     * into the secondary summary when both are present.
     */
    @Test
    public void organizerListAdapter_prefersFullNameAndContactSummary() {
        ContextThemeWrapper context = themedContext();
        ArrayList<User> entrants = new ArrayList<>();
        User user = new User(
                "entrant-device-1",
                "Nadia",
                "Stone",
                "nadia_s",
                "nadia@example.com",
                "7801234567",
                false
        );
        entrants.add(user);
        OrganizerListAdapter adapter = new OrganizerListAdapter(
                context,
                R.layout.item_organizer_list,
                entrants,
                "Accepted",
                true
        );
        ViewGroup parent = new FrameLayout(context);

        View row = adapter.getView(0, null, parent);

        assertEquals("Nadia Stone", text(row, R.id.entrant_name_text));
        assertEquals("nadia@example.com • 7801234567", text(row, R.id.entrant_detail_text));
        assertEquals("Accepted", text(row, R.id.entrant_status_chip));
    }

    /**
     * Verifies that organizer rows fall back to the entrant device id when profile details are
     * missing.
     */
    @Test
    public void organizerListAdapter_fallsBackToDeviceIdWhenProfileDetailsAreMissing() {
        ContextThemeWrapper context = themedContext();
        ArrayList<User> entrants = new ArrayList<>();
        User user = new User();
        user.setDeviceId("entrant-device-2");
        entrants.add(user);
        OrganizerListAdapter adapter = new OrganizerListAdapter(
                context,
                R.layout.item_organizer_list,
                entrants,
                "Waiting",
                false
        );
        ViewGroup parent = new FrameLayout(context);

        View row = adapter.getView(0, null, parent);

        assertEquals("entrant-device-2", text(row, R.id.entrant_name_text));
        assertEquals("entrant-device-2", text(row, R.id.entrant_detail_text));
        assertEquals("Waiting", text(row, R.id.entrant_status_chip));
    }

    /**
     * Creates a themed context so organizer rows inflate with the same resources used in-app.
     *
     * @return Application context wrapped in the Breezeseas theme.
     */
    private ContextThemeWrapper themedContext() {
        return new ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(),
                R.style.Theme_Breezeseas
        );
    }

    /**
     * Reads the rendered text from a row subview for concise binding assertions.
     *
     * @param row Inflated organizer row returned by the adapter.
     * @param viewId TextView id within the row.
     * @return Current text rendered in the requested subview.
     */
    private String text(View row, int viewId) {
        return ((android.widget.TextView) row.findViewById(viewId)).getText().toString();
    }
}
