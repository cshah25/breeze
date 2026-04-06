package com.example.breeze_seas;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Small image-binding helper for list rows that need Firestore-backed posters or profile photos.
 *
 * <p>This keeps image loading logic in one place, adds lightweight in-memory caching, and guards
 * against recycled views receiving stale async image callbacks.
 */
public final class UiImageBinder {

    /**
     * Renders the placeholder state for one image slot before any async data arrives.
     */
    public interface PlaceholderRenderer {
        void renderPlaceholder();
    }

    private interface LoadedImageConsumer {
        void onImageLoaded(@Nullable Image image);
    }

    private static final String EMPTY = "__empty__";
    private static final Map<String, String> EVENT_IMAGE_DOC_ID_CACHE = new HashMap<>();
    private static final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    private UiImageBinder() {
    }

    /**
     * Binds one event poster into the supplied image view using the event id as the lookup key.
     *
     * @param imageView Image view that should show the event poster.
     * @param eventId Firestore event document id.
     * @param placeholderRenderer Renderer applied immediately and again when no poster exists.
     */
    public static void bindEventPoster(
            @NonNull ImageView imageView,
            @Nullable String eventId,
            @NonNull PlaceholderRenderer placeholderRenderer
    ) {
        String normalizedEventId = normalize(eventId);
        String requestKey = "event:" + (normalizedEventId == null ? "" : normalizedEventId);
        imageView.setTag(requestKey);
        placeholderRenderer.renderPlaceholder();

        if (normalizedEventId == null) {
            return;
        }

        String cachedImageDocId = EVENT_IMAGE_DOC_ID_CACHE.get(normalizedEventId);
        if (EMPTY.equals(cachedImageDocId)) {
            return;
        }
        if (cachedImageDocId != null) {
            bindImageDocInternal(imageView, cachedImageDocId, requestKey, placeholderRenderer, null);
            return;
        }

        DBConnector.getDb()
                .collection("events")
                .document(normalizedEventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!requestKey.equals(imageView.getTag())) {
                        return;
                    }

                    String imageDocId = documentSnapshot == null
                            ? null
                            : normalize(documentSnapshot.getString("imageDocId"));
                    EVENT_IMAGE_DOC_ID_CACHE.put(normalizedEventId, imageDocId == null ? EMPTY : imageDocId);

                    if (imageDocId != null) {
                        bindImageDocInternal(imageView, imageDocId, requestKey, placeholderRenderer, null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (requestKey.equals(imageView.getTag())) {
                        placeholderRenderer.renderPlaceholder();
                    }
                });
    }

    /**
     * Binds one already-known image document id into the supplied image view.
     *
     * @param imageView Image view that should show the image.
     * @param imageDocId Firestore image document id.
     * @param placeholderRenderer Renderer applied immediately and again when no image exists.
     */
    public static void bindImageDoc(
            @NonNull ImageView imageView,
            @Nullable String imageDocId,
            @NonNull PlaceholderRenderer placeholderRenderer
    ) {
        String normalizedImageDocId = normalize(imageDocId);
        String requestKey = "image:" + (normalizedImageDocId == null ? "" : normalizedImageDocId);
        imageView.setTag(requestKey);
        placeholderRenderer.renderPlaceholder();

        if (normalizedImageDocId == null) {
            return;
        }

        bindImageDocInternal(imageView, normalizedImageDocId, requestKey, placeholderRenderer, null);
    }

    /**
     * Binds one user's avatar image into the supplied image view.
     *
     * @param imageView Image view that should show the avatar.
     * @param user User whose avatar should be displayed.
     * @param placeholderRenderer Renderer applied immediately and again when no avatar exists.
     */
    public static void bindUserAvatar(
            @NonNull ImageView imageView,
            @Nullable User user,
            @NonNull PlaceholderRenderer placeholderRenderer
    ) {
        String normalizedDeviceId = user == null ? null : normalize(user.getDeviceId());
        String requestKey = "user:" + (normalizedDeviceId == null ? "" : normalizedDeviceId);
        imageView.setTag(requestKey);
        placeholderRenderer.renderPlaceholder();

        if (user == null) {
            return;
        }

        Image profileImage = user.getProfileImage();
        if (profileImage != null && profileImage.display() != null) {
            cacheImage(profileImage);
            if (requestKey.equals(imageView.getTag())) {
                applyLoadedImage(imageView, profileImage);
            }
            return;
        }

        String imageDocId = normalize(user.getImageDocId());
        if (imageDocId == null) {
            return;
        }

        bindImageDocInternal(imageView, imageDocId, requestKey, placeholderRenderer, user::setProfileImage);
    }

    private static void bindImageDocInternal(
            @NonNull ImageView imageView,
            @NonNull String imageDocId,
            @NonNull String requestKey,
            @NonNull PlaceholderRenderer placeholderRenderer,
            @Nullable LoadedImageConsumer imageConsumer
    ) {
        Image cachedImage = IMAGE_CACHE.get(imageDocId);
        if (cachedImage != null && cachedImage.display() != null) {
            if (imageConsumer != null) {
                imageConsumer.onImageLoaded(cachedImage);
            }
            if (requestKey.equals(imageView.getTag())) {
                applyLoadedImage(imageView, cachedImage);
            }
            return;
        }

        ImageDB.loadImage(imageDocId, new ImageDB.LoadImageCallback() {
            @Override
            public void onSuccess(Image image) {
                if (imageConsumer != null) {
                    imageConsumer.onImageLoaded(image);
                }
                if (image != null && image.display() != null) {
                    cacheImage(image);
                    if (requestKey.equals(imageView.getTag())) {
                        applyLoadedImage(imageView, image);
                    }
                    return;
                }

                if (requestKey.equals(imageView.getTag())) {
                    placeholderRenderer.renderPlaceholder();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (requestKey.equals(imageView.getTag())) {
                    placeholderRenderer.renderPlaceholder();
                }
            }
        });
    }

    private static void applyLoadedImage(@NonNull ImageView imageView, @NonNull Image image) {
        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(image.display());
        imageView.setImageTintList(null);
    }

    private static void cacheImage(@NonNull Image image) {
        String imageId = normalize(image.getImageId());
        if (imageId != null) {
            IMAGE_CACHE.put(imageId, image);
        }
    }

    @Nullable
    private static String normalize(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
