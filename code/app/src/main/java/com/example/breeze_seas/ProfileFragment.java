package com.example.breeze_seas;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

/*** ProfileFragment is a top-level destination accessed via Bottom Navigation.
 *
 * <p>Current state:* - A placeholder screen was created to validate the navigation wiring and theme style.
 ** <p>Outstanding/Future Work:* - Set up entrant profile viewing/editing and notification choices.
 */
public class ProfileFragment extends Fragment {

    private TextInputEditText editName;


    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

}


