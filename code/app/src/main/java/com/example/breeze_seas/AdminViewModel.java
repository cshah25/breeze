package com.example.breeze_seas;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for admin screens.
 *
 * <p>A separate ViewModel from {@link SessionViewModel} because admin data (images, events,
 * profiles) has a different ownership and lifetime than the current-user session state.
 * Mixing them into SessionViewModel would make it responsible for too many unrelated things
 * and would leave Firestore listeners running even when the user is not in the admin section.</p>
 *
 * <p>This ViewModel is scoped to the activity, so admin data and listeners persist across
 * admin fragment navigation (e.g. going into an image detail screen and coming back) without
 * requiring a re-fetch. Listeners are started on first use and stopped when the ViewModel
 * is cleared (i.e. when the activity finishes).</p>
 */
public class AdminViewModel extends ViewModel {

    private final MutableLiveData<List<Image>> images = new MutableLiveData<>(new ArrayList<>());
    private boolean imagesListening = false;

    /**
     * Returns the live image list. Observe this in admin image fragments.
     */
    public MutableLiveData<List<Image>> getImages() {
        return images;
    }

    /**
     * Starts the real-time listener for the images collection if not already running.
     * Safe to call multiple times — only attaches once.
     */
    public void startImagesListen() {
        if (imagesListening) return;
        imagesListening = true;

        ImageDB.startImagesListen(new ImageDB.ImagesChangedCallback() {
            @Override
            public void onImageAdded(Image image) {
                List<Image> current = images.getValue();
                current.add(image);
                images.setValue(current);
            }

            @Override
            public void onImageModified(Image image) {
                List<Image> current = images.getValue();
                for (int i = 0; i < current.size(); i++) {
                    if (image.getImageId().equals(current.get(i).getImageId())) {
                        current.set(i, image);
                        break;
                    }
                }
                images.setValue(current);
            }

            @Override
            public void onImageRemoved(Image image) {
                List<Image> current = images.getValue();
                current.removeIf(img -> img.getImageId().equals(image.getImageId()));
                images.setValue(current);
            }

            @Override
            public void onFailure(Exception e) {
                // Leave the existing list in place — transient errors shouldn't wipe the UI
            }
        });
    }

    /**
     * Deletes an image from Firestore. The listener will automatically remove it from
     * the live list once Firestore confirms the deletion.
     *
     * @param image The image to delete.
     * @param callback Fired on success or failure.
     */
    public void deleteImage(Image image, ImageDB.ImageMutationCallback callback) {
        ImageDB.deleteImage(image.getImageId(), callback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Stop the listener when the activity is finished to avoid leaks
        ImageDB.stopImagesListen();
        imagesListening = false;
    }
}
