package com.example.breeze_seas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class SendAnnouncementFragment extends Fragment {

    private AppBarLayout appBarLayout;
    private TextInputLayout notificationTextBox;
    private MaterialButton sendButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_send_announcement,
                container, false);

        appBarLayout = view.findViewById(R.id.app_bar_layout);
        notificationTextBox = view.findViewById(R.id.notification_text_box);
        sendButton = view.findViewById(R.id.send_button);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NotificationService notification;

        sendButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Announcement Sent!(TODO)",
                    Toast.LENGTH_SHORT).show();
        });
    }
}
