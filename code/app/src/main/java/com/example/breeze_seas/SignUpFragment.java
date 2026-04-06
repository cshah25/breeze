package com.example.breeze_seas;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestoreException;

/**
 * SignUpFragment is a {@link Fragment} subclass.
 * This fragment contains views for the user to fill in their details.
 * Checks values before proceeding to ExploreFragment.
 */
public class SignUpFragment extends Fragment {

    private SessionViewModel viewModel;
    private String androidID;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
    @Nullable
    private AnimatorSet entranceAnimator;

    public SignUpFragment() {
        super(R.layout.fragment_sign_up);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup viewModel
        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Grab android ID
        this.androidID = viewModel.getAndroidID().getValue();

        // Bind views
        EditText firstNameInput = view.findViewById(R.id.signup_firstname_input);
        EditText lastNameInput = view.findViewById(R.id.signup_lastname_input);
        EditText userNameInput = view.findViewById(R.id.signup_username_input);
        EditText emailInput = view.findViewById(R.id.signup_email_input);
        EditText phoneNumberInput = view.findViewById(R.id.signup_phone_number_input);
        NestedScrollView scrollView = view.findViewById(R.id.signup_scroll_view);
        View headerPanel = view.findViewById(R.id.signup_header_panel);
        View formPanel = view.findViewById(R.id.signup_form_panel);
        View backButton = view.findViewById(R.id.signup_back_button);

        playEntrance(headerPanel, formPanel);
        installFocusAwareScroll(
                scrollView,
                firstNameInput,
                lastNameInput,
                userNameInput,
                emailInput,
                phoneNumberInput
        );

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, new WelcomeScreenFragment())
                .commit());

        Button confirmButton = view.findViewById(R.id.signup_confirm_button);
        confirmButton.setOnClickListener(onClick -> {
            this.firstName = safeText(firstNameInput);
            this.lastName = safeText(lastNameInput);
            this.userName = safeText(userNameInput);
            this.email = safeText(emailInput);
            this.phoneNumber = safeText(phoneNumberInput);

            if (verifyDetails()) {
                User user = new User(this.androidID, this.firstName, this.lastName, this.userName,
                        this.email, this.phoneNumber, false);

                confirmButton.setEnabled(false);
                UserDB userDBInstance = new UserDB();
                userDBInstance.createUser(user)
                        .addOnSuccessListener(unused -> {
                            if (!isAdded()) {
                                return;
                            }

                            // Add user to view model
                            viewModel.setUser(user);

                            MainActivity activity = (MainActivity) requireActivity();
                            activity.showBottomNav(true);
                            activity.navigateToTopLevelDestination(R.id.nav_explore);
                        })
                        .addOnFailureListener(e -> {
                            if (!isAdded()) {
                                return;
                            }

                            confirmButton.setEnabled(true);
                            dialogMsg(buildSignUpErrorMessage(e));
                        });
            }
        });

    }

    @Override
    public void onDestroyView() {
        if (entranceAnimator != null) {
            entranceAnimator.cancel();
            entranceAnimator = null;
        }
        super.onDestroyView();
    }

    /**
     * Checks the details are valid before allowing user to continue.
     * @return True or False whether details meet requirements
     */
    private boolean verifyDetails() {
        // Verifies all fields
        if (!verifyFirstName()) {
            return false;
        } else if (!verifyLastName()) {
            return false;
        } else if (!verifyUserName()) {
            return false;
        } else if (!verifyEmail()) {
            return false;
        } else if (!verifyPhoneNumber()) {
            return false;
        }
        return true;
    }

    /**
     * Checks first name
     * @return True if first name is valid, otherwise False
     */
    private boolean verifyFirstName() {
        if (this.firstName == null || this.firstName.isBlank()) {
            dialogMsg("First Name cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Checks last name
     * @return True if last name is valid, otherwise False
     */
    private boolean verifyLastName() {
        if (this.lastName == null || this.lastName.isBlank()) {
            dialogMsg("Last Name cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Checks user name
     * @return True if user name is valid, otherwise False
     */
    private boolean verifyUserName() {
        if (this.userName == null || this.userName.isBlank()) {
            dialogMsg("User Name cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Checks email address
     * @return True if email address is valid, otherwise False
     */
    private boolean verifyEmail() {
        if (this.email == null || this.email.isBlank()) {
            dialogMsg("Email cannot be empty");
            return false;
        } else if (!this.email.contains("@")) {
            dialogMsg("Email address must contain @");
            return false;
        }
        return true;
    }

    /**
     * Checks phone number
     * @return True if phone number is valid, otherwise False
     */
    private boolean verifyPhoneNumber() {
        // Phone Number is optional, can be blank
        return true;
    }

    /**
     * Takes a string a builds an alert dialogue displaying the message.
     * @param msg Message to show
     */
    private void dialogMsg(String msg) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Profile setup");
        builder.setMessage(msg);
        builder.setPositiveButton("I understand", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @NonNull
    private String safeText(@NonNull EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void playEntrance(@NonNull View headerPanel, @NonNull View formPanel) {
        headerPanel.setAlpha(0f);
        headerPanel.setTranslationY(30f);
        formPanel.setAlpha(0f);
        formPanel.setTranslationY(30f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(headerPanel, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(headerPanel, View.TRANSLATION_Y, 30f, 0f),
                ObjectAnimator.ofFloat(formPanel, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(formPanel, View.TRANSLATION_Y, 30f, 0f)
        );
        set.setDuration(550L);
        set.start();
        entranceAnimator = set;
    }

    private void installFocusAwareScroll(@NonNull NestedScrollView scrollView,
                                         @NonNull EditText... inputs) {
        for (EditText input : inputs) {
            input.setOnFocusChangeListener((focusedView, hasFocus) -> {
                if (!hasFocus) {
                    return;
                }

                scrollView.post(() -> scrollFieldIntoView(scrollView, focusedView));
            });

            input.setOnClickListener(clickedView ->
                    scrollView.post(() -> scrollFieldIntoView(scrollView, clickedView)));
        }
    }

    private void scrollFieldIntoView(@NonNull NestedScrollView scrollView, @NonNull View target) {
        Rect rect = new Rect();
        target.getDrawingRect(rect);
        scrollView.offsetDescendantRectToMyCoords(target, rect);
        scrollView.smoothScrollTo(0, Math.max(0, rect.top - dpToPx(24)));
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String buildSignUpErrorMessage(Exception e) {
        String baseMessage = "Failed to create your account.";

        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return baseMessage + "\n\nFirestore denied the write. Check your Firestore rules.";
            }
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                return baseMessage + "\n\nFirestore is unavailable right now. Check your connection and try again.";
            }
        }

        if (e instanceof FirebaseNetworkException) {
            return baseMessage + "\n\nNetwork error: " + e.getMessage();
        }

        if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return baseMessage + "\n\nDetails: " + e.getMessage();
        }

        return baseMessage + "\n\nCheck Firebase setup and Firestore rules.";
    }
}
