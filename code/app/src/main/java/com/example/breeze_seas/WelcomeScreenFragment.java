package com.example.breeze_seas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * WelcomeScreenFragment is a {@link Fragment} subclass.
 * This fragment is displayed upon a user first opening up the app.
 * Contains a button to lead to the SignUpFragment.
 */
public class WelcomeScreenFragment extends Fragment {

    public WelcomeScreenFragment() {
        super(R.layout.fragment_welcome_screen);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startButton = view.findViewById(R.id.welcome_screen_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignUpFragment signUpFragment = new SignUpFragment();

                if (getActivity() == null) {
                    return;
                }

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                        )
                        .replace(R.id.fragment_container, signUpFragment)
                        .commit();
            }
        });
    }
}
