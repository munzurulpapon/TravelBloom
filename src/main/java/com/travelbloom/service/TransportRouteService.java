package com.travelbloom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Route + fare finder between two locations.
 *
 * HONEST NOTE (same situation as HotelService): a truly free, key-less API
 * that returns real multi-modal routes (bus/train/flight) with live ticket
 * prices for arbitrary origin/destination pairs does not exist — providers
 * like Rome2Rio and Google Distance Matrix require a paid plan.
 *
 * So, exactly like HotelService does for hotels, this returns a realistic,
 * clearly-labelled ESTIMATE for each transport mode (based on straight-line
 * distance and typical per-km fares in Bangladesh), so the feature works
 * today with zero setup. To go live later: register a paid key for one of
 * those providers and replace estimateRoutes() below with a real HTTP call,
 * the same way WeatherService/LocationService already call Open-Meteo —
 * the method signature and return type won't need to change.
 */
public class TransportRouteService {

    private final LocationService locationService = new LocationService();

    public List<TransportOption> findRoutes(String from, String to) throws Exception {

        var fromLocation = locationService.getLocation(from);
        var toLocation = locationService.getLocation(to);

        double distanceKm = haversineKm(
                fromLocation.getLatitude(), fromLocation.getLongitude(),
                toLocation.getLatitude(), toLocation.getLongitude()
        );

        List<TransportOption> options = new ArrayList<>();

        // Bus — always available, cheapest, slowest
        options.add(new TransportOption(
                "Bus", Math.max(1, Math.round(distanceKm / 45.0)),
                round(distanceKm * 1.8), "hrs"
        ));

        // Train — only worth suggesting for medium/long distances
        if (distanceKm > 60) {
            options.add(new TransportOption(
                    "Train", Math.max(1, Math.round(distanceKm / 55.0)),
                    round(distanceKm * 1.5), "hrs"
            ));
        }

        // Private car / rental
        options.add(new TransportOption(
                "Car Rental", Math.max(1, Math.round(distanceKm / 60.0)),
                round(distanceKm * 12.0), "hrs"
        ));

        // Flight — only worth suggesting for long distances
        if (distanceKm > 250) {
            options.add(new TransportOption(
                    "Flight", 1,
                    round(3500 + distanceKm * 6.0), "hrs (+ airport time)"
            ));
        }

        return options;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {

        double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private double round(double value) {
        return Math.round(value / 10.0) * 10.0;
    }


    public static class TransportOption {

        private final String mode;
        private final long durationValue;
        private final double estimatedPrice;
        private final String durationUnit;

        public TransportOption(String mode, long durationValue, double estimatedPrice, String durationUnit) {
            this.mode = mode;
            this.durationValue = durationValue;
            this.estimatedPrice = estimatedPrice;
            this.durationUnit = durationUnit;
        }

        public String getMode() { return mode; }
        public double getEstimatedPrice() { return estimatedPrice; }

        public String getDurationText() {
            return "~" + durationValue + " " + durationUnit;
        }

        public String getPriceText() {
            return String.format(Locale.ROOT, "৳ %.0f (est.)", estimatedPrice);
        }
    }
}
