package com.travelbloom.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Central place to switch between FXML pages.
 *
 * THE BUG THIS FIXES: every controller in the app used to do
 *      Stage stage = (Stage) someButton.getScene().getWindow();
 *      stage.setScene(new Scene(root, 1200, 750));
 * on every single navigation. That hard-codes the window back to
 * 1200x750 every time you click a nav button — so if you'd gone
 * fullscreen or resized the window, clicking to another page snapped
 * it back to the small fixed size.
 *
 * SceneManager instead re-uses the existing Scene and just swaps its
 * root node, and explicitly re-applies whatever maximized/fullscreen
 * state the stage was already in. Width/height (for a normal, not
 * maximized/fullscreen window) are preserved automatically because we
 * never create a new Scene with a fixed size after the first one.
 */
public class SceneManager {

    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 650;

    /**
     * Loads fxmlPath into the given stage, preserving its current size /
     * maximized / fullscreen state. Returns the new page's controller so
     * the caller can call setTrip(...) etc. on it, or null on failure.
     */
    public static <T> T switchScene(Stage stage, String fxmlPath, String title) {

        try {

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            boolean wasMaximized = stage.isMaximized();
            boolean wasFullScreen = stage.isFullScreen();

            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root, 1200, 750);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            stage.setResizable(true);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);

            // Re-apply state (setRoot alone can drop fullscreen on some
            // platforms) instead of letting it silently reset.
            stage.setMaximized(wasMaximized);
            stage.setFullScreen(wasFullScreen);

            if (title != null) {
                stage.setTitle(title);
            }

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
