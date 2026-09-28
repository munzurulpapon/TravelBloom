package com.travelbloom.service;

/**
 * Reads the Makcorps API key from an environment variable so it never
 * gets hardcoded/committed to Git.
 *
 * SET IT (IntelliJ): Run > Edit Configurations > Main > Environment variables
 *   MAKCORPS_API_KEY=your_key
 *
 * SET IT (terminal): export MAKCORPS_API_KEY=your_key
 *
 * If not set, isConfigured() returns false and HotelService automatically
 * falls back to the offline estimator.
 *
 * NOTE: The free "Demo / Test Pack" plan only includes 30 total API calls
 * for the whole 30-day trial (see makcorps.com pricing). Each "Find Hotels"
 * click in this app costs 2 calls (1 mapping lookup + 1 city search), so
 * don't spam the button while testing.
 */
public class MakcorpsConfig {

    private static final String API_KEY = System.getenv("MAKCORPS_API_KEY");

    public static final String BASE_URL = "https://api.makcorps.com";

    public static boolean isConfigured() {
        return API_KEY != null && !API_KEY.isBlank();
    }

    public static String getApiKey() {
        return API_KEY;
    }
}