package com.travelbloom.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL =
            "jdbc:sqlite:travelbloom.db";


    // =========================================
    // DATABASE CONNECTION
    // =========================================

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(URL);
    }


    // =========================================
    // INITIALIZE DATABASE
    // =========================================

    public static void initializeDatabase() {


        // =====================================
        // TRIPS TABLE
        // =====================================

        String tripsSql = """
                CREATE TABLE IF NOT EXISTS trips (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    destination TEXT NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    budget REAL NOT NULL,
                    description TEXT
                )
                """;


        // =====================================
        // ITINERARY TABLE
        // =====================================

        String itinerarySql = """
                CREATE TABLE IF NOT EXISTS itinerary_activities (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    day_label TEXT NOT NULL,
                    time TEXT,
                    title TEXT NOT NULL,
                    location TEXT,
                    notes TEXT,
                    FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
                )
                """;


        // =====================================
        // TRANSPORT TABLE
        // =====================================

        String transportSql = """
                CREATE TABLE IF NOT EXISTS transport (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trip_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    from_location TEXT NOT NULL,
                    to_location TEXT NOT NULL,
                    transport_date TEXT NOT NULL,
                    transport_time TEXT,
                    cost REAL NOT NULL,
                    notes TEXT,
                    FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
                )
                """;


        // =====================================
        // CONNECT TO DATABASE
        // =====================================

        try (
                Connection connection = getConnection();

                Statement statement =
                        connection.createStatement()
        ) {


            // =================================
            // CREATE TRIPS TABLE
            // =================================

            statement.execute(tripsSql);


            // =================================
            // CREATE ITINERARY TABLE
            // =================================

            statement.execute(itinerarySql);


            // =================================
            // CREATE TRANSPORT TABLE
            // =================================

            statement.execute(transportSql);


            // =================================
            // HANDLE OLD ITINERARY DATABASE
            // =================================

            try {

                statement.execute(
                        "ALTER TABLE itinerary_activities " +
                                "ADD COLUMN location TEXT"
                );

            } catch (SQLException ignored) {

                // location column already exists
            }


            System.out.println(
                    "Database initialized successfully!"
            );


        } catch (SQLException e) {

            System.out.println(
                    "Database initialization failed!"
            );

            e.printStackTrace();
        }
    }
}