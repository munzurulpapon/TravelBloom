package com.travelbloom.dao;

import com.travelbloom.model.Stay;
import com.travelbloom.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StayDAO {

    public StayDAO() {
        createTable();
    }

    private void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS stays (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    hotel_name TEXT NOT NULL,
                    location TEXT,
                    check_in TEXT,
                    check_out TEXT,
                    cost REAL,
                    notes TEXT
                )
                """;

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addStay(Stay stay) {

        String sql = """
                INSERT INTO stays
                (trip_id, hotel_name, location, check_in, check_out, cost, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, stay.getTripId());
            pstmt.setString(2, stay.getHotelName());
            pstmt.setString(3, stay.getLocation());
            pstmt.setString(4, stay.getCheckIn());
            pstmt.setString(5, stay.getCheckOut());
            pstmt.setDouble(6, stay.getCost());
            pstmt.setString(7, stay.getNotes());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Stay> getStaysByTripId(int tripId) {

        List<Stay> stays = new ArrayList<>();

        String sql = """
                SELECT *
                FROM stays
                WHERE trip_id = ?
                ORDER BY check_in
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tripId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Stay stay = new Stay();

                stay.setId(rs.getInt("id"));
                stay.setTripId(rs.getInt("trip_id"));
                stay.setHotelName(rs.getString("hotel_name"));
                stay.setLocation(rs.getString("location"));
                stay.setCheckIn(rs.getString("check_in"));
                stay.setCheckOut(rs.getString("check_out"));
                stay.setCost(rs.getDouble("cost"));
                stay.setNotes(rs.getString("notes"));

                stays.add(stay);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stays;
    }

    public void updateStay(Stay stay) {

        String sql = """
                UPDATE stays
                SET hotel_name = ?,
                    location = ?,
                    check_in = ?,
                    check_out = ?,
                    cost = ?,
                    notes = ?
                WHERE id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stay.getHotelName());
            pstmt.setString(2, stay.getLocation());
            pstmt.setString(3, stay.getCheckIn());
            pstmt.setString(4, stay.getCheckOut());
            pstmt.setDouble(5, stay.getCost());
            pstmt.setString(6, stay.getNotes());
            pstmt.setInt(7, stay.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStay(int id) {

        String sql = "DELETE FROM stays WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // NEW: TOTAL STAY COST FOR ONE TRIP
    // (used by Smart Planner & Dashboard)
    // =========================================

    public double getTotalStayCostByTripId(int tripId) {

        String sql = """
                SELECT COALESCE(SUM(cost), 0)
                FROM stays
                WHERE trip_id = ?
                """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tripId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}