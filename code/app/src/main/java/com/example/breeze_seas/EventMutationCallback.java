package com.example.breeze_seas;

/**
 * Callback used by legacy organizer mutation paths that expect success/failure notifications.
 */
public interface EventMutationCallback {

    /**
     * Called after an event mutation succeeds.
     */
    void onSuccess();

    /**
     * Called after an event mutation fails.
     *
     * @param e Failure returned by the attempted mutation.
     */
    void onFailure(Exception e);
}
