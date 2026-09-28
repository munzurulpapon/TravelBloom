package com.travelbloom.dao;

import com.travelbloom.database.Database;
import com.travelbloom.model.PackingItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PackingDAO {

    public PackingDAO() {
        createTable();
    }


    // =========================================
    // CREATE TABLE
    // =========================================

    private void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS packing_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    category TEXT,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    packed INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
                )
                """;

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // ADD ITEM
    // =========================================

    public void addItem(PackingItem item) {

        String sql = """
                INSERT INTO packing_items
                (trip_id, item_name, category, quantity, packed)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, item.getTripId());
            statement.setString(2, item.getItemName());
            statement.setString(3, item.getCategory());
            statement.setInt(4, item.getQuantity());
            statement.setInt(5, item.isPacked() ? 1 : 0);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // GET ITEMS BY TRIP ID
    // =========================================

    public List<PackingItem> getItemsByTripId(int tripId) {

        List<PackingItem> items = new ArrayList<>();

        String sql = """
                SELECT *
                FROM packing_items
                WHERE trip_id = ?
                ORDER BY packed ASC, category, item_name
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                PackingItem item = new PackingItem(
                        resultSet.getInt("id"),
                        resultSet.getInt("trip_id"),
                        resultSet.getString("item_name"),
                        resultSet.getString("category"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("packed") == 1
                );

                items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }


    // =========================================
    // UPDATE ITEM
    // =========================================

    public void updateItem(PackingItem item) {

        String sql = """
                UPDATE packing_items
                SET item_name = ?,
                    category = ?,
                    quantity = ?,
                    packed = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, item.getItemName());
            statement.setString(2, item.getCategory());
            statement.setInt(3, item.getQuantity());
            statement.setInt(4, item.isPacked() ? 1 : 0);
            statement.setInt(5, item.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // TOGGLE PACKED STATUS
    // =========================================

    public void togglePacked(int id, boolean packed) {

        String sql = "UPDATE packing_items SET packed = ? WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, packed ? 1 : 0);
            statement.setInt(2, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // DELETE ITEM
    // =========================================

    public void deleteItem(int id) {

        String sql = "DELETE FROM packing_items WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // PACKED / TOTAL COUNT (for progress display)
    // =========================================

    public int getPackedCount(int tripId) {

        String sql = "SELECT COUNT(*) FROM packing_items WHERE trip_id = ? AND packed = 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getTotalCount(int tripId) {

        String sql = "SELECT COUNT(*) FROM packing_items WHERE trip_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}