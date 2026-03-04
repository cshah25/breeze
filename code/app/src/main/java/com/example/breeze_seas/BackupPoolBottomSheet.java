package com.example.breeze_seas;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * BackupPoolBottomSheet informs the user they are in the backup pool (not selected yet).
 *
 * <p>Supports US 01.05.01: user understands they may still be selected if others decline.</p>
 */
public class BackupPoolBottomSheet extends BottomSheetDialogFragment {
    public BackupPoolBottomSheet() { }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottomsheet_backup_pool, container, false);

        Button ok = v.findViewById(R.id.backup_sheet_ok);
        ok.setOnClickListener(view -> dismiss());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            int height = (int) (requireContext().getResources().getDisplayMetrics().heightPixels * 0.5);
            bottomSheet.getLayoutParams().height = height;
            bottomSheet.requestLayout();
        }
    }
}
