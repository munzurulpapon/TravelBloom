package com.travelbloom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetches destination-aware photos from the Pexels API (free tier: 200
 * requests/hour, no cost). Key is read from the PEXELS_API_KEY
 * environment variable — see ImageConfig.java. Get a free key at
 * https://www.pexels.com/api/
 *
 * If the key isn't set, or the API call fails for any reason, this
 * class never throws — it just returns an empty list, and callers fall
 * back to a plain gradient background (see .trip-photo-card-fallback
 * in style.css).
 */
public class ImageService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // destination|count -> list of photo URLs, cached for the app's lifetime
    private static final Map<String, List<String>> CACHE = new ConcurrentHashMap<>();

    /**
     * One representative photo URL for a destination (trip card background).
     * Returns null if unavailable — caller should keep a gradient fallback.
     */
    public String getPhotoUrl(String destination) {
        List<String> photos = getPhotos(destination, 1);
        return photos.isEmpty() ? null : photos.get(0);
    }

    /**
     * Several photo URLs for a destination (Trip Details slideshow).
     * Never returns null — an empty list means "use the fallback gradient".
     */
    public List<String> getPhotos(String destination, int count) {

        String query = (destination == null || destination.isBlank())
                ? "travel destination"
                : destination.trim();

        String cacheKey = query.toLowerCase() + "|" + count;

        List<String> cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<String> results = new ArrayList<>();

        if (!ImageConfig.isConfigured()) {
            CACHE.put(cacheKey, results);
            return results;
        }

        try {

            String encoded = URLEncoder.encode(query + " travel", StandardCharsets.UTF_8);

            String url = "https://api.pexels.com/v1/search?query=" + encoded
                    + "&per_page=" + Math.max(count, 1)
                    + "&orientation=landscape";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", ImageConfig.getApiKey())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {

                JsonNode root = objectMapper.readTree(response.body());
                JsonNode photos = root.get("photos");

                if (photos != null) {

                    for (JsonNode photo : photos) {

                        JsonNode src = photo.get("src");

                        if (src == null) continue;

                        if (src.get("large2x") != null) {
                            results.add(src.get("large2x").asText());
                        } else if (src.get("large") != null) {
                            results.add(src.get("large").asText());
                        }
                    }
                }

            } else {

                System.out.println(
                        "Pexels API request failed. Status: " + response.statusCode()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        CACHE.put(cacheKey, results);
        return results;
    }


    // =========================================================
    // DOWNLOAD RAW IMAGE BYTES
    // =========================================================

    /**
     * Downloads the raw bytes of an image from any URL (e.g. a photo URL
     * returned by getPhotoUrl/getPhotos). Useful when you need the image
     * as byte[] instead of a URL string — for example embedding it in an
     * email (base64), saving it to disk, or building a JavaFX Image from
     * an InputStream without JavaFX re-downloading it itself.
     *
     * Returns null (never throws) if the URL is blank or the download
     * fails for any reason — callers should handle null gracefully.
     */
    public byte[] downloadImageBytes(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofByteArray()
            );

            if (response.statusCode() != 200) {
                System.out.println(
                        "Image download failed. Status: " + response.statusCode()
                                + " for URL: " + imageUrl
                );
                return null;
            }

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}