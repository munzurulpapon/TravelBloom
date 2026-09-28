package com.travelbloom.dao;

import com.travelbloom.database.Database;
import com.travelbloom.model.Trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TripDAO {

    // CREATE
    public void saveTrip(Trip trip) {

        String sql = """
                INSERT INTO trips
                (title, destination, start_date, end_date, budget, description)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, trip.getTitle());
            statement.setString(2, trip.getDestination());
            statement.setString(3, trip.getStartDate());
            statement.setString(4, trip.getEndDate());
            statement.setDouble(5, trip.getBudget());
            statement.setString(6, trip.getDescription());

            statement.executeUpdate();

            System.out.println("Trip saved successfully!");

        } catch (Exception e) {

            System.out.println("Failed to save trip!");

            e.printStackTrace();
        }
    }


    // READ
    public List<Trip> getAllTrips() {

        List<Trip> trips = new ArrayList<>();

        String sql = """
                SELECT *
                FROM trips
                ORDER BY id DESC
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                Trip trip = new Trip(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("destination"),
                        resultSet.getString("start_date"),
                        resultSet.getString("end_date"),
                        resultSet.getDouble("budget"),
                        resultSet.getString("description")
                );

                trips.add(trip);
            }

        } catch (Exception e) {

            System.out.println("Failed to load trips!");

            e.printStackTrace();
        }

        return trips;
    }


    // UPDATE
    public void updateTrip(Trip trip) {

        String sql = """
                UPDATE trips
                SET title = ?,
                    destination = ?,
                    start_date = ?,
                    end_date = ?,
                    budget = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, trip.getTitle());
            statement.setString(2, trip.getDestination());
            statement.setString(3, trip.getStartDate());
            statement.setString(4, trip.getEndDate());
            statement.setDouble(5, trip.getBudget());
            statement.setString(6, trip.getDescription());
            statement.setInt(7, trip.getId());

            statement.executeUpdate();

            System.out.println("Trip updated successfully!");

        } catch (Exception e) {

            System.out.println("Failed to update trip!");

            e.printStackTrace();
        }
    }


    // DELETE
    public void deleteTrip(int tripId) {

        String sql = """
                DELETE FROM trips
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            statement.executeUpdate();

            System.out.println("Trip deleted successfully!");

        } catch (Exception e) {

            System.out.println("Failed to delete trip!");

            e.printStackTrace();
        }
    }
}