package com.travelbloom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbloom.model.LocationData;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LocationService {

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public LocationService() {

        httpClient = HttpClient.newHttpClient();

        objectMapper = new ObjectMapper();
    }

    public LocationData getLocation(
            String destination)
            throws IOException, InterruptedException {

        String encodedDestination =
                URLEncoder.encode(
                        destination,
                        StandardCharsets.UTF_8
                );

        String url =
                "https://geocoding-api.open-meteo.com/v1/search"
                        + "?name=" + encodedDestination
                        + "&count=1"
                        + "&language=en"
                        + "&format=json";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() != 200) {

            throw new IOException(
                    "Location API request failed. Status code: "
                            + response.statusCode()
            );
        }

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );

        JsonNode results =
                root.get("results");

        if (results == null
                || results.isEmpty()) {

            throw new IOException(
                    "Location not found for: "
                            + destination
            );
        }

        JsonNode firstResult =
                results.get(0);

        String name =
                firstResult
                        .get("name")
                        .asText();

        double latitude =
                firstResult
                        .get("latitude")
                        .asDouble();

        double longitude =
                firstResult
                        .get("longitude")
                        .asDouble();

        return new LocationData(
                name,
                latitude,
                longitude
        );
    }
}