package com.example.breeze_seas;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Shared QR and confirmation-ticket screen used by both organizer and entrant flows.
 */
public class ViewQrCodeFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_IS_CREATED = "isCreated";
    private static final String ARG_CONFIRMATION_MODE = "confirmationMode";
    private static final String ARG_TICKET_TITLE = "ticketTitle";
    private static final String ARG_TICKET_DATE_LABEL = "ticketDateLabel";
    private static final String ARG_TICKET_TYPE_LABEL = "ticketTypeLabel";

    private SessionViewModel viewModel;
    @Nullable
    private Bitmap currentQrBitmap;
    @Nullable
    private String currentEventName;
    @Nullable
    private String currentTicketId;
    private boolean confirmationMode;

    private TextView titleView;
    private TextView subtitleView;
    private TextView ticketChipView;
    private TextView ticketCardEventNameView;
    private TextView ticketCardLeadView;
    private LinearLayout ticketMetadataGroup;
    private TextView ticketHolderValueView;
    private TextView ticketDateValueView;
    private TextView ticketStatusValueView;
    private TextView ticketIdValueView;
    private ImageView qrImageView;
    private Button saveButton;
    private Button manageEntrantsButton;
    private View ticketExportCard;

    private final androidx.activity.result.ActivityResultLauncher<String> savePermissionLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (!isAdded()) {
                            return;
                        }

                        if (isGranted) {
                            saveCurrentAssetToDevice();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    R.string.view_qr_code_save_permission_denied,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    public ViewQrCodeFragment() {
        super(R.layout.fragment_view_qr_code);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);
        bindViews(view);
        saveButton.setOnClickListener(v -> maybeSaveCurrentAsset());
        view.findViewById(R.id.btnClose).setOnClickListener(v -> handleClose());

        Bundle args = getArguments();
        confirmationMode = args != null && args.getBoolean(ARG_CONFIRMATION_MODE, false);

        if (confirmationMode) {
            bindConfirmationTicket(args);
            return;
        }

        boolean isCreated = args != null && args.getBoolean(ARG_IS_CREATED, false);
        bindOrganizerQr(args, isCreated);
    }

    private void bindViews(@NonNull View view) {
        titleView = view.findViewById(R.id.tvTitle);
        subtitleView = view.findViewById(R.id.tvEventName);
        ticketChipView = view.findViewById(R.id.tvTicketChip);
        ticketCardEventNameView = view.findViewById(R.id.tvTicketCardEventName);
        ticketCardLeadView = view.findViewById(R.id.tvTicketCardLead);
        ticketMetadataGroup = view.findViewById(R.id.ticketMetadataGroup);
        ticketHolderValueView = view.findViewById(R.id.tvTicketHolderValue);
        ticketDateValueView = view.findViewById(R.id.tvTicketDateValue);
        ticketStatusValueView = view.findViewById(R.id.tvTicketStatusValue);
        ticketIdValueView = view.findViewById(R.id.tvTicketIdValue);
        qrImageView = view.findViewById(R.id.ivQr);
        saveButton = view.findViewById(R.id.btnSaveQr);
        manageEntrantsButton = view.findViewById(R.id.btnManageEntrants);
        ticketExportCard = view.findViewById(R.id.ticketExportCard);
    }

    private void handleClose() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return;
        }

        Fragment fallbackFragment = resolveCloseFallbackFragment();
        if (fallbackFragment == null) {
            return;
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fallbackFragment)
                .commit();
    }

    @Nullable
    private Fragment resolveCloseFallbackFragment() {
        if (confirmationMode) {
            return new TicketsFragment();
        }

        Bundle args = getArguments();
        boolean isCreated = args != null && args.getBoolean(ARG_IS_CREATED, false);
        Event currentEvent = viewModel == null ? null : viewModel.getEventShown().getValue();
        if (currentEvent == null) {
            return null;
        }

        return isCreated ? new OrganizerEventPreviewFragment() : new EventDetailsFragment();
    }

    private void bindOrganizerQr(@Nullable Bundle args, boolean isCreated) {
        titleView.setVisibility(isCreated ? VISIBLE : INVISIBLE);
        titleView.setText(R.string.view_qr_code_created_title);
        subtitleView.setText(
                isCreated
                        ? R.string.view_qr_code_created_subtitle
                        : R.string.view_qr_code_public_subtitle
        );
        ticketChipView.setText(R.string.view_qr_code_public_chip);
        ticketCardLeadView.setText(R.string.view_qr_code_public_card_support);
        ticketMetadataGroup.setVisibility(GONE);
        manageEntrantsButton.setVisibility(isCreated ? VISIBLE : GONE);
        saveButton.setText(R.string.view_qr_code_save_button);

        String eventId = args == null ? null : args.getString(ARG_EVENT_ID);
        if (eventId == null) {
            ticketCardEventNameView.setText(R.string.view_qr_code_unknown_event);
            return;
        }

        EventDB.getEventById(eventId, new EventDB.LoadSingleEventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (!isAdded()) {
                    return;
                }

                if (event == null) {
                    ticketCardEventNameView.setText(R.string.view_qr_code_unknown_event);
                    return;
                }

                currentEventName = event.getName();
                ticketCardEventNameView.setText(
                        sanitizeText(event.getName(), getString(R.string.view_qr_code_unknown_event))
                );
                currentQrBitmap = makeQr("event:" + event.getEventId());
                qrImageView.setImageBitmap(currentQrBitmap);
                manageEntrantsButton.setOnClickListener(v -> openManageEntrantsFragment(event));
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) {
                    return;
                }
                ticketCardEventNameView.setText(R.string.view_qr_code_load_error);
            }
        });
    }

    private void bindConfirmationTicket(@Nullable Bundle args) {
        titleView.setVisibility(VISIBLE);
        titleView.setText(R.string.view_qr_code_ticket_title);
        subtitleView.setText(R.string.view_qr_code_ticket_subtitle);
        ticketMetadataGroup.setVisibility(VISIBLE);
        manageEntrantsButton.setVisibility(GONE);
        saveButton.setText(R.string.view_qr_code_ticket_save_button);

        String eventId = args == null ? null : args.getString(ARG_EVENT_ID);
        String title = args == null ? null : args.getString(ARG_TICKET_TITLE);
        String dateLabel = args == null ? null : args.getString(ARG_TICKET_DATE_LABEL);
        String ticketType = args == null ? null : args.getString(ARG_TICKET_TYPE_LABEL);

        currentEventName = sanitizeText(title, getString(R.string.view_qr_code_unknown_event));
        ticketCardEventNameView.setText(currentEventName);
        ticketChipView.setText(
                sanitizeText(ticketType, getString(R.string.view_qr_code_ticket_status_confirmed))
        );
        ticketCardLeadView.setText(R.string.view_qr_code_ticket_card_support);

        String holderName = buildCurrentUserLabel();
        String userId = buildCurrentUserId();
        String ticketId = buildTicketId(eventId, userId);
        currentTicketId = ticketId;

        ticketHolderValueView.setText(holderName);
        ticketDateValueView.setText(
                sanitizeText(dateLabel, getString(R.string.organizer_event_preview_not_set))
        );
        ticketStatusValueView.setText(R.string.view_qr_code_ticket_status_confirmed);
        ticketIdValueView.setText(ticketId);

        currentQrBitmap = makeQr(buildTicketPayload(eventId, ticketId, userId, holderName, dateLabel));
        qrImageView.setImageBitmap(currentQrBitmap);
    }

    private void maybeSaveCurrentAsset() {
        if (currentQrBitmap == null) {
            Toast.makeText(requireContext(), R.string.view_qr_code_save_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveCurrentAssetToDevice();
            return;
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED) {
            saveCurrentAssetToDevice();
        } else {
            savePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void saveCurrentAssetToDevice() {
        try {
            if (confirmationMode) {
                saveTicketPdf();
                Toast.makeText(
                        requireContext(),
                        R.string.view_qr_code_ticket_save_success,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                saveOrganizerQrPng();
                Toast.makeText(
                        requireContext(),
                        R.string.view_qr_code_save_success,
                        Toast.LENGTH_SHORT
                ).show();
            }
        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    confirmationMode
                            ? R.string.view_qr_code_ticket_save_failure
                            : R.string.view_qr_code_save_failure,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void saveOrganizerQrPng() throws Exception {
        Bitmap qrBitmap = currentQrBitmap;
        if (qrBitmap == null) {
            throw new IllegalStateException("QR bitmap unavailable.");
        }

        String fileName = buildOrganizerQrFileName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveQrWithMediaStore(qrBitmap, fileName);
        } else {
            saveQrLegacy(qrBitmap, fileName);
        }
    }

    private void saveTicketPdf() throws Exception {
        View exportView = ticketExportCard;
        if (exportView == null) {
            throw new IllegalStateException("Ticket card unavailable.");
        }

        PdfDocument document = buildTicketPdf(exportView);
        try {
            String fileName = buildTicketPdfFileName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                savePdfWithMediaStore(document, fileName);
            } else {
                savePdfLegacy(document, fileName);
            }
        } finally {
            document.close();
        }
    }

    @NonNull
    private PdfDocument buildTicketPdf(@NonNull View exportView) {
        int exportWidth = exportView.getWidth();
        int exportHeight = exportView.getHeight();
        if (exportWidth <= 0 || exportHeight <= 0) {
            int desiredWidth = getResources().getDisplayMetrics().widthPixels - dpToPx(40);
            int widthSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            exportView.measure(widthSpec, heightSpec);
            exportWidth = exportView.getMeasuredWidth();
            exportHeight = exportView.getMeasuredHeight();
            exportView.layout(0, 0, exportWidth, exportHeight);
        }

        int pageWidth = 595;
        int margin = 32;
        float scale = (pageWidth - (margin * 2f)) / exportWidth;
        int scaledHeight = Math.round(exportHeight * scale);
        int pageHeight = Math.max(842, scaledHeight + (margin * 2));

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawColor(Color.WHITE);
        canvas.save();
        canvas.translate(margin, margin);
        canvas.scale(scale, scale);
        exportView.draw(canvas);
        canvas.restore();
        document.finishPage(page);
        return document;
    }

    private void openManageEntrantsFragment(@NonNull Event event) {
        try {
            Class<?> fragmentClass = Class.forName("com.example.breeze_seas.ManageEntrantsFragment");
            Object instance = fragmentClass.getDeclaredConstructor().newInstance();
            if (!(instance instanceof Fragment)) {
                throw new IllegalStateException("ManageEntrantsFragment is not a Fragment");
            }

            Fragment fragment = (Fragment) instance;
            if (viewModel != null) {
                viewModel.setEventShown(event);
            }
            ((MainActivity) requireActivity()).openSecondaryFragment(fragment);
        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    R.string.organizer_event_preview_manage_unavailable,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @NonNull
    private String buildOrganizerQrFileName() {
        return "breeze_qr_" + sanitizeFileComponent(currentEventName) + "_" + System.currentTimeMillis() + ".png";
    }

    @NonNull
    private String buildTicketPdfFileName() {
        String ticketId = currentTicketId == null ? "ticket" : currentTicketId;
        return "breeze_ticket_" + sanitizeFileComponent(currentEventName) + "_" + ticketId + ".pdf";
    }

    private void saveQrWithMediaStore(@NonNull Bitmap qrBitmap, @NonNull String fileName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Breeze"
        );
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        Uri imageUri = requireContext()
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri == null) {
            throw new IllegalStateException("Unable to create MediaStore entry.");
        }

        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(imageUri)) {
            if (outputStream == null || !qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new IllegalStateException("Unable to write QR image.");
            }
        }

        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        requireContext().getContentResolver().update(imageUri, values, null, null);
    }

    private void saveQrLegacy(@NonNull Bitmap qrBitmap, @NonNull String fileName) throws Exception {
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDirectory = new File(picturesDirectory, "Breeze");
        if (!appDirectory.exists() && !appDirectory.mkdirs()) {
            throw new IllegalStateException("Unable to create image directory.");
        }

        File outputFile = new File(appDirectory, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            if (!qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                throw new IllegalStateException("Unable to write QR image.");
            }
        }

        MediaScannerConnection.scanFile(
                requireContext(),
                new String[]{outputFile.getAbsolutePath()},
                new String[]{"image/png"},
                null
        );
    }

    private void savePdfWithMediaStore(@NonNull PdfDocument document, @NonNull String fileName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/Breeze"
        );
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri fileUri = requireContext()
                .getContentResolver()
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (fileUri == null) {
            throw new IllegalStateException("Unable to create PDF download entry.");
        }

        try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(fileUri)) {
            if (outputStream == null) {
                throw new IllegalStateException("Unable to write PDF ticket.");
            }
            document.writeTo(outputStream);
        }

        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        requireContext().getContentResolver().update(fileUri, values, null, null);
    }

    private void savePdfLegacy(@NonNull PdfDocument document, @NonNull String fileName) throws Exception {
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File appDirectory = new File(downloadsDirectory, "Breeze");
        if (!appDirectory.exists() && !appDirectory.mkdirs()) {
            throw new IllegalStateException("Unable to create download directory.");
        }

        File outputFile = new File(appDirectory, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            document.writeTo(outputStream);
        }

        MediaScannerConnection.scanFile(
                requireContext(),
                new String[]{outputFile.getAbsolutePath()},
                new String[]{"application/pdf"},
                null
        );
    }

    @NonNull
    private String buildCurrentUserLabel() {
        User currentUser = viewModel == null ? null : viewModel.getUser().getValue();
        if (currentUser != null) {
            String firstName = sanitizeText(currentUser.getFirstName(), "");
            String lastName = sanitizeText(currentUser.getLastName(), "");
            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isEmpty()) {
                return fullName;
            }

            String userName = sanitizeText(currentUser.getUserName(), "");
            if (!userName.isEmpty()) {
                return userName;
            }

            String email = sanitizeText(currentUser.getEmail(), "");
            if (!email.isEmpty()) {
                return email;
            }
        }

        return getString(R.string.view_qr_code_ticket_holder_fallback);
    }

    @NonNull
    private String buildCurrentUserId() {
        User currentUser = viewModel == null ? null : viewModel.getUser().getValue();
        if (currentUser != null) {
            String deviceId = sanitizeText(currentUser.getDeviceId(), "");
            if (!deviceId.isEmpty()) {
                return deviceId;
            }
        }

        String androidId = viewModel == null ? null : viewModel.getAndroidID().getValue();
        return sanitizeText(androidId, "unknown-device");
    }

    @NonNull
    private String buildTicketId(@Nullable String eventId, @NonNull String userId) {
        String seed = sanitizeText(eventId, "unknown-event") + "|" + userId;
        String digest = sha256(seed);
        return "BS-" + digest.substring(0, Math.min(10, digest.length())).toUpperCase(Locale.US);
    }

    @NonNull
    private String buildTicketPayload(
            @Nullable String eventId,
            @NonNull String ticketId,
            @NonNull String userId,
            @NonNull String holderName,
            @Nullable String dateLabel
    ) {
        return "breeze-confirmation-ticket"
                + "\nTicket ID: " + ticketId
                + "\nEvent ID: " + sanitizeText(eventId, "unknown-event")
                + "\nEntrant ID: " + userId
                + "\nEntrant: " + holderName
                + "\nEvent: " + sanitizeText(currentEventName, getString(R.string.view_qr_code_unknown_event))
                + "\nDate: " + sanitizeText(dateLabel, getString(R.string.organizer_event_preview_not_set));
    }

    @NonNull
    private String sha256(@NonNull String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                builder.append(String.format(Locale.US, "%02x", current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(value.hashCode());
        }
    }

    @NonNull
    private String sanitizeFileComponent(@Nullable String rawValue) {
        String value = sanitizeText(rawValue, "event");
        value = value.replaceAll("[^a-zA-Z0-9_-]+", "_");
        return value.isEmpty() ? "event" : value;
    }

    @NonNull
    private String sanitizeText(@Nullable String value, @NonNull String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private int dpToPx(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @NonNull
    private Bitmap makeQr(@NonNull String content) {
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 800, 800);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
