package com.travelbloom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds "famous places near here" using Wikipedia's GeoSearch API
 * (https://en.wikipedia.org/w/api.php?action=query&list=geosearch...).
 * This is completely free and needs NO API key at all — it just returns
 * nearby Wikipedia articles, which in practice are almost always the
 * well-known landmarks/attractions of a place (temples, beaches, forts,
 * museums, parks, etc).
 */
public class PlacesService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class Place {
        public final String name;
        public final double distanceMeters;

        public Place(String name, double distanceMeters) {
            this.name = name;
            this.distanceMeters = distanceMeters;
        }
    }

    /**
     * Returns up to `limit` nearby notable places, ordered by distance.
     * Never throws — returns an empty list if the lookup fails for any
     * reason (offline, unrecognized coordinates, etc).
     */
    public List<Place> findNearbyPlaces(double latitude, double longitude, int limit) {

        List<Place> places = new ArrayList<>();

        try {

            String url = "https://en.wikipedia.org/w/api.php"
                    + "?action=query&list=geosearch"
                    + "&gscoord=" + latitude + "%7C" + longitude
                    + "&gsradius=10000"
                    + "&gslimit=" + Math.max(limit, 1)
                    + "&format=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "TravelBloom/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                return places;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode results = root.path("query").path("geosearch");

            for (JsonNode result : results) {

                String title = result.path("title").asText(null);
                double dist = result.path("dist").asDouble(0);

                if (title != null) {
                    places.add(new Place(title, dist));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return places;
    }
}
