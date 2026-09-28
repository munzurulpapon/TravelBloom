package com.travelbloom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Talks to the real Makcorps Hotel Price API (makcorps.com).
 *
 * Two calls are needed per search (this is how Makcorps' API is designed
 * — there's no single "give me hotels for this destination text" call):
 *
 *   1) GET /mapping   destination text  -> city ID  (type == "GEO")
 *   2) GET /city       city ID          -> up to ~30 hotels w/ cheapest
 *                                          vendor prices for that page
 *
 * Each of those two calls counts as 1 request against the free 30-call
 * quota, so a single "Find Hotels" click costs 2 calls total.
 */
public class MakcorpsHotelClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<LiveHotelOffer> searchLiveHotels(String destination, LocalDate checkIn, LocalDate checkOut)
            throws IOException, InterruptedException {

        String cityId = findCityId(destination);

        if (cityId == null) {
            return List.of(); // couldn't resolve a city -> caller falls back to estimator
        }

        return searchHotelsByCity(cityId, checkIn, checkOut);
    }


    // =========================================================
    // STEP 1: destination text -> city ID
    // =========================================================

    private String findCityId(String destination) throws IOException, InterruptedException {

        String url = MakcorpsConfig.BASE_URL + "/mapping"
                + "?api_key=" + URLEncoder.encode(MakcorpsConfig.getApiKey(), StandardCharsets.UTF_8)
                + "&name=" + URLEncoder.encode(destination, StandardCharsets.UTF_8);

        JsonNode root = getJson(url);

        if (root == null || !root.isArray()) {
            return null;
        }

        for (JsonNode entry : root) {

            JsonNode type = entry.get("type");
            JsonNode documentId = entry.get("document_id");

            if (type != null && "GEO".equals(type.asText()) && documentId != null) {
                return documentId.asText();
            }
        }

        return null; // no city (GEO) match found in the response
    }


    // =========================================================
    // STEP 2: city ID -> live hotels + cheapest prices
    // =========================================================

    private List<LiveHotelOffer> searchHotelsByCity(String cityId, LocalDate checkIn, LocalDate checkOut)
            throws IOException, InterruptedException {

        String url = MakcorpsConfig.BASE_URL + "/city"
                + "?cityid=" + URLEncoder.encode(cityId, StandardCharsets.UTF_8)
                + "&pagination=0"
                + "&cur=USD"
                + "&rooms=1"
                + "&adults=1"
                + "&checkin=" + checkIn
                + "&checkout=" + checkOut
                + "&api_key=" + URLEncoder.encode(MakcorpsConfig.getApiKey(), StandardCharsets.UTF_8);

        JsonNode root = getJson(url);

        List<LiveHotelOffer> offers = new ArrayList<>();

        if (root == null || !root.isArray()) {
            return offers;
        }

        for (JsonNode entry : root) {

            // The API's last array element is pagination metadata wrapped
            // in its own array (see docs) — it has no "hotelId", skip it.
            if (entry.get("hotelId") == null || entry.get("name") == null) {
                continue;
            }

            String name = entry.get("name").asText();

            Double cheapestPrice = null;
            String cheapestVendor = null;

            // vendor1/price1, vendor2/price2, ... up to vendor4/price4 (docs)
            for (int i = 1; i <= 4; i++) {

                JsonNode priceNode = entry.get("price" + i);
                JsonNode vendorNode = entry.get("vendor" + i);

                if (priceNode == null || priceNode.isNull() || vendorNode == null) {
                    continue;
                }

                Double parsed = parsePrice(priceNode.asText());

                if (parsed != null && (cheapestPrice == null || parsed < cheapestPrice)) {
                    cheapestPrice = parsed;
                    cheapestVendor = vendorNode.asText();
                }
            }

            if (cheapestPrice == null) {
                continue; // no usable price for this hotel, skip it
            }

            double rating = 0;
            JsonNode reviews = entry.get("reviews");
            if (reviews != null && reviews.get("rating") != null) {
                rating = reviews.get("rating").asDouble();
            }

            offers.add(new LiveHotelOffer(name, cheapestPrice, "USD", cheapestVendor, rating));
        }

        return offers;
    }


    // Turns "$1,443" or "1443" into 1443.0. Returns null if unparsable.
    private Double parsePrice(String raw) {

        if (raw == null || raw.isBlank()) {
            return null;
        }

        String digitsOnly = raw.replaceAll("[^0-9.]", "");

        if (digitsOnly.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(digitsOnly);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    // =========================================================
    // SHARED GET HELPER
    // =========================================================

    private JsonNode getJson(String url) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 404) {
            // Makcorps uses 404 for "no data" or "bad key" — treat as empty, not fatal
            return null;
        }

        if (response.statusCode() != 200) {
            throw new IOException("Makcorps API call failed (" + url + "). Status: "
                    + response.statusCode() + " Body: " + response.body());
        }

        return objectMapper.readTree(response.body());
    }


    // =========================================================
    // SIMPLE RESULT MODEL
    // =========================================================

    public static class LiveHotelOffer {

        private final String name;
        private final double price;
        private final String currency;
        private final String vendor;
        private final double rating; // 0 when unknown

        public LiveHotelOffer(String name, double price, String currency, String vendor, double rating) {
            this.name = name;
            this.price = price;
            this.currency = currency;
            this.vendor = vendor;
            this.rating = rating;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getCurrency() { return currency; }
        public String getVendor() { return vendor; }
        public double getRating() { return rating; }
    }
}