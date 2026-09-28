package com.travelbloom.util;

import com.travelbloom.service.ImageService;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;

/**
 * Shared helper so every page (Itinerary, Transport, Stay, Expense,
 * Packing, ...) loads its trip-location background photo the exact same
 * way, instead of each controller re-implementing the background thread.
 *
 * Usage pattern in each FXML: a StackPane root (fx:id="rootPane") whose
 * first child is an ImageView (fx:id="backgroundImage", preserveRatio
 * false) with its fitWidth/fitHeight bound to rootPane in initialize(),
 * followed by a dark gradient Region overlay, followed by the real content.
 */
public class BackgroundPhotoUtil {

    private static final ImageService imageService = new ImageService();

    /**
     * Fetches one destination photo in the background and applies it to
     * imageView on the JavaFX thread once ready. Does nothing (keeps
     * whatever fallback color is already set in CSS) if the Pexels key
     * isn't configured yet, the request fails, or the CDN rejects the
     * download (Pexels 403s bare requests without a browser User-Agent,
     * so this downloads the bytes manually via ImageService instead of
     * handing a raw URL straight to javafx.scene.image.Image).
     */
    public static void applyDestinationBackground(ImageView imageView, String destination) {

        Thread thread = new Thread(() -> {

            String url = imageService.getPhotoUrl(destination);

            if (url == null) return;

            byte[] bytes = imageService.downloadImageBytes(url);

            if (bytes == null) return;

            Image image = new Image(new ByteArrayInputStream(bytes));

            if (!image.isError()) {
                Platform.runLater(() -> imageView.setImage(image));
            } else {
                System.out.println(
                        "Failed to decode background photo: " + url + " -> " + image.getException()
                );
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}