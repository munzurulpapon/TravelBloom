package com.travelbloom.service;

import com.travelbloom.model.LocationData;
import com.travelbloom.model.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the chatbot's final trip plan: real weather (Open-Meteo, free),
 * real nearby landmarks (Wikipedia GeoSearch, free), a day-by-day
 * itinerary skeleton shaped by who's travelling, and rule-based shopping
 * tips (same honest-estimate pattern as PriceEstimatorService — there is
 * no free "local shopping guide" API, so this uses destination keywords
 * instead of pretending to be a live source).
 */
public class TripPlanReportService {

    private final LocationService locationService = new LocationService();
    private final WeatherService weatherService = new WeatherService();
    private final PlacesService placesService = new PlacesService();

    public static class TripPlanReport {
        public String weatherSummary;
        public List<String> famousPlaces = new ArrayList<>();
        public List<String> itineraryDays = new ArrayList<>();
        public List<String> shoppingTips = new ArrayList<>();
    }

    public TripPlanReport generateReport(
            String destination, int days, String companionType) throws Exception {

        TripPlanReport report = new TripPlanReport();

        LocationData location = locationService.getLocation(destination);

        // ---------- Weather ----------
        try {

            WeatherData weather = weatherService.getWeather(
                    location.getLatitude(), location.getLongitude()
            );

            report.weatherSummary = String.format(
                    Locale.ROOT,
                    "Right now it's about %.0f°C with wind around %.0f km/h — %s.",
                    weather.getTemperature(), weather.getWindSpeed(),
                    describeWeatherCode(weather.getWeatherCode())
            );

        } catch (Exception e) {
            report.weatherSummary = "Couldn't fetch live weather for " + destination + " right now.";
        }

        // ---------- Famous places ----------
        List<PlacesService.Place> places =
                placesService.findNearbyPlaces(location.getLatitude(), location.getLongitude(), 8);

        for (PlacesService.Place place : places) {
            report.famousPlaces.add(place.name + " (~" + Math.round(place.distanceMeters) + "m away)");
        }

        // ---------- Itinerary skeleton ----------
        report.itineraryDays = buildItinerary(destination, days, companionType, places);

        // ---------- Shopping tips ----------
        report.shoppingTips = buildShoppingTips(destination, companionType);

        return report;
    }


    private List<String> buildItinerary(
            String destination, int days, String companionType, List<PlacesService.Place> places) {

        List<String> itinerary = new ArrayList<>();

        String vibe = switch (companionType == null ? "" : companionType.toLowerCase()) {
            case "honeymoon" -> "a relaxed, romantic pace — sunsets, quiet cafés, couple spa time";
            case "family" -> "family-friendly stops with breaks, and kid-safe activities";
            case "friends" -> "an active pace — adventure spots, nightlife, group activities";
            default -> "a flexible, easygoing pace";
        };

        for (int day = 1; day <= Math.max(days, 1); day++) {

            StringBuilder line = new StringBuilder("Day " + day + ": ");

            if (day == 1) {
                line.append("Arrive in ").append(destination)
                        .append(", settle in, and take an easy first look around (")
                        .append(vibe).append(").");
            } else if (!places.isEmpty()) {

                int placeIndex = (day - 2) % places.size();
                line.append("Visit ").append(places.get(placeIndex).name);

                if (places.size() > 1) {
                    int secondIndex = (day - 1) % places.size();
                    if (secondIndex != placeIndex) {
                        line.append(", then ").append(places.get(secondIndex).name);
                    }
                }

                line.append(".");

            } else {
                line.append("Explore local neighborhoods and try the regional food.");
            }

            itinerary.add(line.toString());
        }

        return itinerary;
    }


    private List<String> buildShoppingTips(String destination, String companionType) {

        List<String> tips = new ArrayList<>();

        String dest = destination == null ? "" : destination.toLowerCase(Locale.ROOT);

        if (dest.contains("cox") || dest.contains("bazar") || dest.contains("beach")) {
            tips.add("🐚 Look for local seashell crafts and dried seafood at the beach market stalls.");
        }

        if (dest.contains("sylhet") || dest.contains("srimangal")) {
            tips.add("🍵 Sylhet/Srimangal is known for its tea — pick up local tea gardens' packaged tea as souvenirs.");
        }

        if (dest.contains("rangamati") || dest.contains("bandarban") || dest.contains("hill")) {
            tips.add("🧵 Look for handwoven tribal textiles and bamboo handicrafts from local markets.");
        }

        if (dest.contains("dhaka")) {
            tips.add("👗 New Market and Bailey Road are popular for clothing and jamdani sarees.");
        }

        if ("honeymoon".equalsIgnoreCase(companionType)) {
            tips.add("💍 Consider a small keepsake or matching accessory as a trip memento.");
        }

        // Always-useful general tips
        tips.add("💵 Carry small cash for local markets — many stalls don't take cards.");
        tips.add("🛍 Bargaining is common in local bazaars; asking politely for a discount is normal.");

        return tips;
    }


    private String describeWeatherCode(int code) {

        return switch (code) {
            case 0 -> "clear skies";
            case 1, 2, 3 -> "partly cloudy";
            case 45, 48 -> "foggy";
            case 51, 53, 55, 56, 57 -> "light drizzle";
            case 61, 63, 65, 66, 67 -> "rainy";
            case 71, 73, 75, 77 -> "snowy";
            case 80, 81, 82 -> "rain showers";
            case 85, 86 -> "snow showers";
            case 95 -> "thunderstorms";
            case 96, 99 -> "thunderstorms with hail";
            default -> "mixed conditions";
        };
    }
}
