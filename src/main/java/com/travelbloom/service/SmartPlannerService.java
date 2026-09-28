package com.travelbloom.service;

import com.travelbloom.dao.ExpenseDAO;
import com.travelbloom.dao.ItineraryDAO;
import com.travelbloom.dao.PackingDAO;
import com.travelbloom.dao.StayDAO;
import com.travelbloom.dao.TransportDAO;
import com.travelbloom.model.Trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based "Smart Planner" analysis engine.
 * Looks across everything already stored for a trip (transport, stay,
 * expenses, itinerary, packing) and produces a plain-language report:
 * budget health, missing pieces, and readiness for the trip date.
 *
 * No external AI call is used here — this is deterministic logic over
 * the user's own data, so it works fully offline.
 */
public class SmartPlannerService {

    private final TransportDAO transportDAO = new TransportDAO();
    private final StayDAO stayDAO = new StayDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final ItineraryDAO itineraryDAO = new ItineraryDAO();
    private final PackingDAO packingDAO = new PackingDAO();


    // =========================================================
    // MAIN REPORT
    // =========================================================

    public List<String> generateInsights(Trip trip) {

        List<String> insights = new ArrayList<>();

        if (trip == null) {
            insights.add("No trip selected.");
            return insights;
        }

        addTimelineInsight(trip, insights);
        addBudgetInsight(trip, insights);
        addTransportInsight(trip, insights);
        addStayInsight(trip, insights);
        addItineraryInsight(trip, insights);
        addPackingInsight(trip, insights);

        if (insights.isEmpty()) {
            insights.add("✅ Everything looks great for this trip!");
        }

        return insights;
    }


    // =========================================================
    // TIMELINE
    // =========================================================

    private void addTimelineInsight(Trip trip, List<String> insights) {

        try {

            LocalDate start = LocalDate.parse(trip.getStartDate());
            LocalDate end = LocalDate.parse(trip.getEndDate());
            LocalDate today = LocalDate.now();

            long duration = ChronoUnit.DAYS.between(start, end) + 1;

            if (today.isBefore(start)) {

                long daysLeft = ChronoUnit.DAYS.between(today, start);

                insights.add(
                        "🗓 " + daysLeft + " day(s) left until \""
                                + trip.getTitle() + "\" begins ("
                                + duration + " day trip)."
                );

            } else if (!today.isAfter(end)) {

                insights.add(
                        "📍 \"" + trip.getTitle()
                                + "\" is currently ongoing (" + duration + " days total)."
                );

            } else {

                insights.add(
                        "✔ \"" + trip.getTitle() + "\" has already ended."
                );
            }

        } catch (Exception e) {

            insights.add("⚠ Trip dates could not be parsed for analysis.");
        }
    }


    // =========================================================
    // BUDGET
    // =========================================================

    private void addBudgetInsight(Trip trip, List<String> insights) {

        double budget = trip.getBudget();

        double transportCost = transportDAO.getTotalTransportCost(trip.getId());
        double stayCost = stayDAO.getTotalStayCostByTripId(trip.getId());
        double otherExpenses = expenseDAO.getTotalExpensesByTripId(trip.getId());

        double totalSpent = transportCost + stayCost + otherExpenses;

        double remaining = budget - totalSpent;

        double usedPercent = budget == 0 ? 0 : (totalSpent / budget) * 100;

        if (remaining < 0) {

            insights.add(
                    String.format(
                            "⚠ Over budget by ৳ %.2f (spent ৳ %.2f of ৳ %.2f).",
                            Math.abs(remaining), totalSpent, budget
                    )
            );

        } else if (usedPercent >= 85) {

            insights.add(
                    String.format(
                            "🟠 Budget almost used up — %.0f%% spent (৳ %.2f left).",
                            usedPercent, remaining
                    )
            );

        } else {

            insights.add(
                    String.format(
                            "💰 Budget looking healthy — %.0f%% used, ৳ %.2f remaining.",
                            usedPercent, remaining
                    )
            );
        }
    }


    // =========================================================
    // TRANSPORT
    // =========================================================

    private void addTransportInsight(Trip trip, List<String> insights) {

        int count = transportDAO.getTransportByTripId(trip.getId()).size();

        if (count == 0) {
            insights.add("🚆 No transport has been arranged yet.");
        }
    }


    // =========================================================
    // STAY
    // =========================================================

    private void addStayInsight(Trip trip, List<String> insights) {

        int count = stayDAO.getStaysByTripId(trip.getId()).size();

        if (count == 0) {
            insights.add("🏨 No accommodation has been booked yet.");
        }
    }


    // =========================================================
    // ITINERARY
    // =========================================================

    private void addItineraryInsight(Trip trip, List<String> insights) {

        int count = itineraryDAO.getActivitiesByTrip(trip.getId()).size();

        if (count == 0) {
            insights.add("📅 No itinerary activities have been planned yet.");
        }
    }


    // =========================================================
    // PACKING
    // =========================================================

    private void addPackingInsight(Trip trip, List<String> insights) {

        int total = packingDAO.getTotalCount(trip.getId());
        int packed = packingDAO.getPackedCount(trip.getId());

        if (total == 0) {

            insights.add("🎒 Packing list is empty — add the essentials.");
            return;
        }

        int percent = (int) ((packed / (double) total) * 100);

        try {

            LocalDate start = LocalDate.parse(trip.getStartDate());
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), start);

            if (percent < 100 && daysLeft <= 3 && daysLeft >= 0) {

                insights.add(
                        "🎒 Packing is only " + percent
                                + "% done and the trip starts in " + daysLeft + " day(s)!"
                );

            } else if (percent < 100) {

                insights.add(
                        "🎒 Packing progress: " + packed + "/" + total + " items ("
                                + percent + "%)."
                );
            }

        } catch (Exception ignored) {

            if (percent < 100) {
                insights.add(
                        "🎒 Packing progress: " + packed + "/" + total + " items ("
                                + percent + "%)."
                );
            }
        }
    }
}