package com.travelbloom.service;

/**
 * Pexels API key — hardcoded directly here for simplicity.
 * Free key: https://www.pexels.com/api/
 */
public class ImageConfig {

    // Paste your Pexels API key here
    private static final String API_KEY = "";

    public static boolean isConfigured() {
        return API_KEY != null && !API_KEY.isBlank()
                && !API_KEY.equals("PASTE_YOUR_PEXELS_KEY_HERE");
    }

    public static String getApiKey() {
        return API_KEY;
    }
}