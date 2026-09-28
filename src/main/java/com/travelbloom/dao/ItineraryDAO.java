package com.travelbloom.dao;

import com.travelbloom.database.Database;
import com.travelbloom.model.ItineraryActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ItineraryDAO {


    // =========================================================
    // ADD ACTIVITY
    // =========================================================

    public void addActivity(ItineraryActivity activity) {

        String sql = """
                INSERT INTO itinerary_activities
                (trip_id, day_label, time, title, location, notes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        Database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    activity.getTripId()
            );

            statement.setString(
                    2,
                    activity.getDayLabel()
            );

            statement.setString(
                    3,
                    activity.getTime()
            );

            statement.setString(
                    4,
                    activity.getTitle()
            );

            statement.setString(
                    5,
                    activity.getLocation()
            );

            statement.setString(
                    6,
                    activity.getNotes()
            );


            statement.executeUpdate();


            System.out.println(
                    "Itinerary activity added successfully!"
            );


        } catch (Exception e) {

            System.out.println(
                    "Failed to add itinerary activity."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // GET ACTIVITIES BY TRIP
    // =========================================================

    public List<ItineraryActivity> getActivitiesByTrip(
            int tripId
    ) {

        List<ItineraryActivity> activities =
                new ArrayList<>();


        String sql = """
                SELECT *
                FROM itinerary_activities
                WHERE trip_id = ?
                ORDER BY id
                """;


        try (
                Connection connection =
                        Database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    tripId
            );


            ResultSet resultSet =
                    statement.executeQuery();


            while (resultSet.next()) {

                ItineraryActivity activity =
                        new ItineraryActivity();


                activity.setId(
                        resultSet.getInt("id")
                );

                activity.setTripId(
                        resultSet.getInt("trip_id")
                );

                activity.setDayLabel(
                        resultSet.getString("day_label")
                );

                activity.setTime(
                        resultSet.getString("time")
                );

                activity.setTitle(
                        resultSet.getString("title")
                );

                activity.setLocation(
                        resultSet.getString("location")
                );

                activity.setNotes(
                        resultSet.getString("notes")
                );


                activities.add(activity);
            }


        } catch (Exception e) {

            System.out.println(
                    "Failed to load itinerary activities."
            );

            e.printStackTrace();
        }


        return activities;
    }


    // =========================================================
    // UPDATE ACTIVITY
    // =========================================================

    public void updateActivity(
            ItineraryActivity activity
    ) {

        String sql = """
                UPDATE itinerary_activities
                SET day_label = ?,
                    time = ?,
                    title = ?,
                    location = ?,
                    notes = ?
                WHERE id = ?
                """;


        try (
                Connection connection =
                        Database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    activity.getDayLabel()
            );

            statement.setString(
                    2,
                    activity.getTime()
            );

            statement.setString(
                    3,
                    activity.getTitle()
            );

            statement.setString(
                    4,
                    activity.getLocation()
            );

            statement.setString(
                    5,
                    activity.getNotes()
            );

            statement.setInt(
                    6,
                    activity.getId()
            );


            statement.executeUpdate();


            System.out.println(
                    "Itinerary activity updated successfully!"
            );


        } catch (Exception e) {

            System.out.println(
                    "Failed to update itinerary activity."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // DELETE ACTIVITY
    // =========================================================

    public void deleteActivity(int id) {

        String sql = """
                DELETE FROM itinerary_activities
                WHERE id = ?
                """;


        try (
                Connection connection =
                        Database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    id
            );


            statement.executeUpdate();


            System.out.println(
                    "Itinerary activity deleted successfully!"
            );


        } catch (Exception e) {

            System.out.println(
                    "Failed to delete itinerary activity."
            );

            e.printStackTrace();
        }
    }
}