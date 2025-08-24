package com.papel.imdb_clone.util;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Utility class to detect when the user has stopped typing.
 * This helps optimize search operations by waiting for a pause in typing
 * before triggering the search.
 */
public class PauseTypingDetector {
    private final long delayMs;
    private PauseTransition pause;
    private Runnable onPauseAction;

    /**
     * Creates a new PauseTypingDetector with the specified delay.
     *
     * @param delayMs The delay in milliseconds to wait after typing stops
     *                before triggering the action.
     */
    public PauseTypingDetector(long delayMs) {
        this.delayMs = delayMs;
        this.pause = new PauseTransition(Duration.millis(delayMs));
        this.pause.setOnFinished(event -> {
            if (onPauseAction != null) {
                onPauseAction.run();
            }
        });
    }

    /**
     * Call this method whenever there is user input.
     * This will reset the delay timer.
     */
    public void typing() {
        pause.playFromStart();
    }

    /**
     * Sets the action to be performed when the user stops typing.
     *
     * @param action The action to perform after the typing delay
     */
    public void setOnPause(Runnable action) {
        this.onPauseAction = action;
    }

    /**
     * Convenience method to set the action and start the detector in one call.
     *
     * @param action The action to perform after the typing delay
     */
    public void runOnPause(Runnable action) {
        setOnPause(action);
        typing();
    }

    /**
     * Stops the current delay timer.
     */
    public void stop() {
        pause.stop();
    }
}
