package com.travelbloom.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Gives a realistic estimated price for an expense category at a given
 * destination.
 *
 * IMPORTANT / HONEST NOTE:
 * There is no free, key-less API that returns real-time local prices for
 * arbitrary categories (food, shopping, etc.) at arbitrary destinations.
 * Services that do this (Numbeo, etc.) require a paid plan.
 *
 * This class instead uses a destination "cost index" (relative to a
 * Dhaka/Bangladesh baseline) combined with a base price per category, which
 * gives sensible ballpark numbers offline, with zero setup.
 *
 * To go fully "live": swap the body of estimate() to call a real pricing
 * API (e.g. a paid Numbeo plan, or your own scraped dataset) — the method
 * signature and return type here won't need to change.
 */
public class PriceEstimatorService {

    // Relative cost-of-living multiplier vs. a Dhaka baseline (1.0).
    // Extend this map with more destinations as needed.
    private static final Map<String, Double> COST_INDEX = new HashMap<>();

    static {
        COST_INDEX.put("dhaka", 1.0);
        COST_INDEX.put("cox's bazar", 1.1);
        COST_INDEX.put("sylhet", 0.9);
        COST_INDEX.put("bangkok", 1.6);
        COST_INDEX.put("bali", 1.8);
        COST_INDEX.put("kuala lumpur", 1.5);
        COST_INDEX.put("singapore", 3.2);
        COST_INDEX.put("dubai", 3.5);
        COST_INDEX.put("london", 4.5);
        COST_INDEX.put("paris", 4.3);
        COST_INDEX.put("new york", 5.0);
        COST_INDEX.put("tokyo", 3.8);
        COST_INDEX.put("kathmandu", 0.95);
        COST_INDEX.put("delhi", 1.1);
        COST_INDEX.put("colombo", 1.2);
    }

    // Base price in BDT (৳) at the Dhaka baseline, per category, per day/item.
    private static final Map<String, Double> BASE_PRICE = new HashMap<>();

    static {
        BASE_PRICE.put("food", 600.0);
        BASE_PRICE.put("shopping", 1500.0);
        BASE_PRICE.put("sightseeing", 500.0);
        BASE_PRICE.put("activities", 1200.0);
        BASE_PRICE.put("emergency", 2000.0);
        BASE_PRICE.put("other", 800.0);
    }


    /**
     * @return an estimated cost in BDT for the given category at the given
     * destination. Never fails — falls back to sensible defaults for
     * unknown destinations/categories.
     */
    public double estimate(String destination, String category) {

        double multiplier = COST_INDEX.getOrDefault(
                normalize(destination), 1.3 // unknown destination → assume slightly above local
        );

        double base = BASE_PRICE.getOrDefault(
                normalize(category), 800.0
        );

        return round(base * multiplier);
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private double round(double value) {
        return Math.round(value / 10.0) * 10.0;
    }
}