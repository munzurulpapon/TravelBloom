package com.travelbloom.service;

import com.travelbloom.model.LocationData;
import com.travelbloom.model.Trip;
import com.travelbloom.model.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Suggests packing items based on:
 *  1) Real current weather at the destination (Open-Meteo — free, no key)
 *  2) Simple keyword matching on the trip's destination/description
 *     ("beach", "hiking", "bike", "mountain", "snow", "business", ...)
 *
 * This is a genuinely working, offline-friendly "smart suggestion" —
 * no paid API required.
 */
public class PackingSuggestionService {

    private final LocationService locationService = new LocationService();
    private final WeatherService weatherService = new WeatherService();


    public List<String> suggest(Trip trip) throws Exception {

        List<String> suggestions = new ArrayList<>();

        String destination = trip.getDestination();
        String description = trip.getDescription() == null ? "" : trip.getDescription();

        String context = (destination + " " + description).toLowerCase(Locale.ROOT);


        // ---------- Weather-based ----------

        try {

            LocationData location = locationService.getLocation(destination);

            if (location != null) {

                WeatherData weather = weatherService.getWeather(
                        location.getLatitude(), location.getLongitude()
                );

                if (weather != null) {
                    suggestions.addAll(suggestFromWeather(weather));
                }
            }

        } catch (Exception ignored) {
            // Weather lookup failed (e.g. destination unrecognized) — skip silently,
            // keyword-based suggestions below still work.
        }


        // ---------- Keyword / trip-type based ----------

        if (context.contains("beach") || context.contains("sea") || context.contains("bay")
                || context.contains("cox")) {

            addIfAbsent(suggestions, "🕶 Sunglasses");
            addIfAbsent(suggestions, "🧴 Sunscreen");
            addIfAbsent(suggestions, "👙 Swimwear");
            addIfAbsent(suggestions, "🩴 Flip-flops");
        }

        if (context.contains("hik") || context.contains("trek") || context.contains("mountain")) {

            addIfAbsent(suggestions, "🥾 Hiking boots");
            addIfAbsent(suggestions, "🎒 Trekking backpack");
            addIfAbsent(suggestions, "🧢 Cap/Hat");
        }

        if (context.contains("bike") || context.contains("motorcycle") || context.contains("cycling")) {

            addIfAbsent(suggestions, "⛑ Helmet");
            addIfAbsent(suggestions, "🧤 Riding gloves");
        }

        if (context.contains("snow") || context.contains("winter") || context.contains("cold")) {

            addIfAbsent(suggestions, "🧥 Warm jacket");
            addIfAbsent(suggestions, "🧣 Scarf & gloves");
        }

        if (context.contains("business") || context.contains("conference") || context.contains("meeting")) {

            addIfAbsent(suggestions, "👔 Formal outfit");
            addIfAbsent(suggestions, "💻 Laptop & charger");
        }

        // Always-useful basics
        addIfAbsent(suggestions, "🪪 ID / Passport copies");
        addIfAbsent(suggestions, "🔌 Power bank & charger");
        addIfAbsent(suggestions, "💊 Basic medicine kit");

        return suggestions;
    }


    private List<String> suggestFromWeather(WeatherData weather) {

        List<String> items = new ArrayList<>();

        int code = weather.getWeatherCode();
        double temp = weather.getTemperature();

        if (temp <= 15) {
            items.add("🧥 Warm layer (weather looks cold: " + temp + "°C)");
        } else if (temp >= 32) {
            items.add("💧 Extra water bottle (weather looks hot: " + temp + "°C)");
        }

        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            items.add("☔ Umbrella / raincoat (rain expected)");
        }

        if (code >= 71 && code <= 86) {
            items.add("❄ Snow boots (snow expected)");
        }

        if (code == 0 || (code >= 1 && code <= 3)) {
            items.add("🕶 Sunglasses (clear/sunny skies expected)");
        }

        return items;
    }


    private void addIfAbsent(List<String> list, String item) {
        if (!list.contains(item)) {
            list.add(item);
        }
    }
}