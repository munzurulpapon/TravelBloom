package com.travelbloom.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Hotel search service.
 *
 * Wired to the real Makcorps Hotel Price API (makcorps.com) when
 * MAKCORPS_API_KEY is set as an environment variable (see
 * MakcorpsConfig.java). If it's not set, or the live call fails for any
 * reason (network issue, free-tier quota used up, city not recognized,
 * etc.), this transparently falls back to the destination-aware offline
 * ESTIMATE — so the feature never just breaks.
 *
 * (We previously wired this to Amadeus, but Amadeus fully decommissioned
 * its free Self-Service developer portal on July 17, 2026, so new API
 * keys are no longer obtainable there. Makcorps replaces it here.)
 *
 * NOTE ON THE FREE TIER: Makcorps' free "Demo" plan is only 30 total API
 * calls for a 30-day trial. Each live search here costs 2 of those calls
 * (a city lookup + a hotel list call), so avoid spamming "Find Hotels".
 */
public class HotelService {

    private final PriceEstimatorService priceEstimatorService = new PriceEstimatorService();
    private final MakcorpsHotelClient makcorpsHotelClient = new MakcorpsHotelClient();


    public List<HotelOption> searchHotels(String destination, int minStars) {

        if (MakcorpsConfig.isConfigured()) {

            try {

                List<HotelOption> liveResults = searchHotelsLive(destination, minStars);

                if (!liveResults.isEmpty()) {
                    return liveResults;
                }

                // Makcorps returned nothing usable (e.g. unknown city) -> fall through to estimate

            } catch (Exception e) {

                System.out.println(
                        "Live hotel search failed, falling back to estimate: " + e.getMessage()
                );
            }
        }

        return estimateHotels(destination, minStars);
    }


    // =========================================================
    // LIVE SEARCH (Makcorps)
    // =========================================================

    private List<HotelOption> searchHotelsLive(String destination, int minStars) throws Exception {

        // Makcorps needs real dates; default to a 1-night stay starting
        // tomorrow since this screen doesn't currently pass real
        // check-in/out dates in.
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(1);

        List<MakcorpsHotelClient.LiveHotelOffer> offers =
                makcorpsHotelClient.searchLiveHotels(destination, checkIn, checkOut);

        List<HotelOption> results = new ArrayList<>();

        for (MakcorpsHotelClient.LiveHotelOffer offer : offers) {

            // Makcorps gives a real 0-5 review rating (not a hotel "star"
            // rating), so we use it only as a rough stand-in and can't
            // strictly filter by minStars the same way the estimator does.
            int roughStars = (int) Math.round(offer.getRating());

            if (offer.getRating() > 0 && roughStars < minStars) {
                continue;
            }

            results.add(HotelOption.live(
                    offer.getName(), destination, offer.getPrice(),
                    offer.getCurrency(), offer.getVendor(), roughStars
            ));
        }

        return results;
    }


    // =========================================================
    // OFFLINE ESTIMATOR (fallback — used when no key is set,
    // or the live call fails)
    // =========================================================

    private List<HotelOption> estimateHotels(String destination, int minStars) {

        List<HotelOption> results = new ArrayList<>();

        String cleanDestination = destination == null || destination.isBlank()
                ? "your destination"
                : destination.trim();

        String[] namePrefixes = {
                "Grand", "Royal", "Sunset", "Palm", "Ocean View",
                "City Central", "Heritage", "Blue Lagoon"
        };

        for (int stars = 5; stars >= minStars && stars >= 1; stars--) {

            for (int i = 0; i < 2; i++) {

                String prefix = namePrefixes[(stars * 2 + i) % namePrefixes.length];

                String name = prefix + " Hotel " + cleanDestination;

                double basePricePerNight = 1200; // BDT baseline for a 1-star equivalent
                double starMultiplier = Math.pow(1.6, stars - 1);

                double locationMultiplier =
                        priceEstimatorService.estimate(destination, "other") / 800.0;

                double price = Math.round(
                        (basePricePerNight * starMultiplier * locationMultiplier) / 50.0
                ) * 50.0;

                results.add(HotelOption.estimated(name, cleanDestination, stars, price));
            }
        }

        return results;
    }


    // =========================================================
    // MODEL
    // =========================================================

    public static class HotelOption {

        private final String name;
        private final String location;
        private final int stars;       // 0 when unknown (live offers without a rating)
        private final double pricePerNight;
        private final String currency; // "৳" for estimates, whatever Makcorps returns for live (USD here)
        private final String vendor;   // cheapest OTA name, live results only
        private final boolean live;

        private HotelOption(String name, String location, int stars, double pricePerNight,
                            String currency, String vendor, boolean live) {
            this.name = name;
            this.location = location;
            this.stars = stars;
            this.pricePerNight = pricePerNight;
            this.currency = currency;
            this.vendor = vendor;
            this.live = live;
        }

        public static HotelOption estimated(String name, String location, int stars, double pricePerNight) {
            return new HotelOption(name, location, stars, pricePerNight, "৳", null, false);
        }

        public static HotelOption live(String name, String location, double price,
                                       String currency, String vendor, int stars) {
            return new HotelOption(name, location, stars, price, currency, vendor, true);
        }

        public String getName() { return name; }
        public String getLocation() { return location; }
        public int getStars() { return stars; }
        public double getPricePerNight() { return pricePerNight; }
        public boolean isLive() { return live; }

        public String getStarsText() {
            return stars > 0
                    ? "★".repeat(stars) + "☆".repeat(5 - stars)
                    : "";
        }

        public String getPriceText() {
            if (live) {
                String vendorPart = vendor != null ? " via " + vendor : "";
                return String.format(Locale.ROOT, "%s %.0f / night%s  •  LIVE", currency, pricePerNight, vendorPart);
            }
            return String.format(Locale.ROOT, "%s %.0f / night  •  estimated", currency, pricePerNight);
        }
    }
}