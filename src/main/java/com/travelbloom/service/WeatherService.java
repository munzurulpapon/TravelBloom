package com.travelbloom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbloom.model.WeatherData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    // HTTP client for sending API requests
    private final HttpClient httpClient;

    // Jackson object for reading JSON
    private final ObjectMapper objectMapper;


// =========================================================
// CONSTRUCTOR
// =========================================================

    public WeatherService() {

        httpClient = HttpClient.newHttpClient();

        objectMapper = new ObjectMapper();
    }


// =========================================================
// GET WEATHER
// =========================================================

    public WeatherData getWeather(
            double latitude,
            double longitude)
            throws IOException, InterruptedException {


        // =====================================================
        // CREATE API URL
        // =====================================================

        String url =
                "https://api.open-meteo.com/v1/forecast"
                        + "?latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&current=temperature_2m,weather_code,wind_speed_10m";


        System.out.println(
                "Weather API URL: "
                        + url
        );


        // =====================================================
        // CREATE HTTP REQUEST
        // =====================================================

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();


        // =====================================================
        // SEND REQUEST
        // =====================================================

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );


        // =====================================================
        // CHECK RESPONSE
        // =====================================================

        if (response.statusCode() != 200) {

            throw new IOException(
                    "Weather API request failed. Status code: "
                            + response.statusCode()
            );
        }


        // =====================================================
        // READ JSON RESPONSE
        // =====================================================

        JsonNode root =
                objectMapper.readTree(
                        response.body()
                );


        // =====================================================
        // GET "current" OBJECT
        // =====================================================

        JsonNode current =
                root.get("current");


        if (current == null) {

            throw new IOException(
                    "Weather API response does not contain current weather data."
            );
        }


        // =====================================================
        // GET TEMPERATURE
        // =====================================================

        JsonNode temperatureNode =
                current.get("temperature_2m");


        if (temperatureNode == null) {

            throw new IOException(
                    "Temperature data not found in API response."
            );
        }


        double temperature =
                temperatureNode.asDouble();


        // =====================================================
        // GET WIND SPEED
        // =====================================================

        JsonNode windSpeedNode =
                current.get("wind_speed_10m");


        if (windSpeedNode == null) {

            throw new IOException(
                    "Wind speed data not found in API response."
            );
        }


        double windSpeed =
                windSpeedNode.asDouble();


        // =====================================================
        // GET WEATHER CODE
        // =====================================================

        JsonNode weatherCodeNode =
                current.get("weather_code");


        if (weatherCodeNode == null) {

            throw new IOException(
                    "Weather code not found in API response."
            );
        }


        int weatherCode =
                weatherCodeNode.asInt();


        // =====================================================
        // PRINT WEATHER DATA
        // =====================================================

        System.out.println(
                "Temperature: "
                        + temperature
                        + " °C"
        );

        System.out.println(
                "Wind Speed: "
                        + windSpeed
                        + " km/h"
        );

        System.out.println(
                "Weather Code: "
                        + weatherCode
        );


        // =====================================================
        // RETURN WEATHER DATA
        // =====================================================

        return new WeatherData(
                temperature,
                windSpeed,
                weatherCode
        );
    }
}