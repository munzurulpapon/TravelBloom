package com.travelbloom.dao;

import com.travelbloom.database.Database;
import com.travelbloom.model.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public ExpenseDAO() {
        createTable();
    }


    // =========================================
    // CREATE TABLE (self-healing, like StayDAO)
    // =========================================

    private void createTable() {

        String sql = """
                CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    amount REAL NOT NULL,
                    date TEXT,
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
    // ADD EXPENSE
    // =========================================

    public void addExpense(Expense expense) {

        String sql = """
                INSERT INTO expenses
                (trip_id, category, description, amount, date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, expense.getTripId());
            statement.setString(2, expense.getCategory());
            statement.setString(3, expense.getDescription());
            statement.setDouble(4, expense.getAmount());
            statement.setString(5, expense.getDate());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // GET EXPENSES BY TRIP ID
    // =========================================

    public List<Expense> getExpensesByTripId(int tripId) {

        List<Expense> expenses = new ArrayList<>();

        String sql = """
                SELECT *
                FROM expenses
                WHERE trip_id = ?
                ORDER BY date DESC, id DESC
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {

                Expense expense = new Expense(
                        resultSet.getInt("id"),
                        resultSet.getInt("trip_id"),
                        resultSet.getString("category"),
                        resultSet.getString("description"),
                        resultSet.getDouble("amount"),
                        resultSet.getString("date")
                );

                expenses.add(expense);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expenses;
    }


    // =========================================
    // UPDATE EXPENSE
    // =========================================

    public void updateExpense(Expense expense) {

        String sql = """
                UPDATE expenses
                SET category = ?,
                    description = ?,
                    amount = ?,
                    date = ?
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, expense.getCategory());
            statement.setString(2, expense.getDescription());
            statement.setDouble(3, expense.getAmount());
            statement.setString(4, expense.getDate());
            statement.setInt(5, expense.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // DELETE EXPENSE
    // =========================================

    public void deleteExpense(int id) {

        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // =========================================
    // TOTAL EXPENSES FOR ONE TRIP
    // =========================================

    public double getTotalExpensesByTripId(int tripId) {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE trip_id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    // =========================================
    // TOTAL EXPENSES ACROSS ALL TRIPS
    // (used by Dashboard)
    // =========================================

    public double getTotalExpensesAll() {

        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses";

        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            if (result.next()) {
                return result.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}