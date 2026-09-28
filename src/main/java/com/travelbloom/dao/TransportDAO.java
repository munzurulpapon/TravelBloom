package com.travelbloom.dao;

import com.travelbloom.database.Database;
import com.travelbloom.model.Transport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TransportDAO {


    // =========================================
    // ADD TRANSPORT
    // =========================================

    public void saveTransport(Transport transport) {

        String sql = """
                INSERT INTO transport
                (
                    trip_id,
                    type,
                    from_location,
                    to_location,
                    transport_date,
                    transport_time,
                    cost,
                    notes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        Database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    transport.getTripId()
            );

            statement.setString(
                    2,
                    transport.getType()
            );

            statement.setString(
                    3,
                    transport.getFromLocation()
            );

            statement.setString(
                    4,
                    transport.getToLocation()
            );

            statement.setString(
                    5,
                    transport.getTransportDate()
            );

            statement.setString(
                    6,
                    transport.getTransportTime()
            );

            statement.setDouble(
                    7,
                    transport.getCost()
            );

            statement.setString(
                    8,
                    transport.getNotes()
            );

            statement.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================
    // GET TRANSPORT BY TRIP ID
    // =========================================

    public List<Transport> getTransportByTripId(
            int tripId
    ) {

        List<Transport> transports =
                new ArrayList<>();

        String sql = """
                SELECT *
                FROM transport
                WHERE trip_id = ?
                ORDER BY transport_date, transport_time
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

            ResultSet result =
                    statement.executeQuery();


            while (result.next()) {

                Transport transport =
                        new Transport(

                                result.getInt("id"),

                                result.getInt("trip_id"),

                                result.getString("type"),

                                result.getString(
                                        "from_location"
                                ),

                                result.getString(
                                        "to_location"
                                ),

                                result.getString(
                                        "transport_date"
                                ),

                                result.getString(
                                        "transport_time"
                                ),

                                result.getDouble("cost"),

                                result.getString("notes")
                        );


                transports.add(transport);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return transports;
    }


    // =========================================
    // UPDATE TRANSPORT
    // =========================================

    public void updateTransport(
            Transport transport
    ) {

        String sql = """
                UPDATE transport
                SET
                    type = ?,
                    from_location = ?,
                    to_location = ?,
                    transport_date = ?,
                    transport_time = ?,
                    cost = ?,
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
                    transport.getType()
            );

            statement.setString(
                    2,
                    transport.getFromLocation()
            );

            statement.setString(
                    3,
                    transport.getToLocation()
            );

            statement.setString(
                    4,
                    transport.getTransportDate()
            );

            statement.setString(
                    5,
                    transport.getTransportTime()
            );

            statement.setDouble(
                    6,
                    transport.getCost()
            );

            statement.setString(
                    7,
                    transport.getNotes()
            );

            statement.setInt(
                    8,
                    transport.getId()
            );

            statement.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================
    // DELETE TRANSPORT
    // =========================================

    public void deleteTransport(
            int transportId
    ) {

        String sql =
                "DELETE FROM transport WHERE id = ?";

        try (
                Connection connection =
                        Database.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    transportId
            );

            statement.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================
    // GET TOTAL TRANSPORT COST
    // =========================================

    public double getTotalTransportCost(
            int tripId
    ) {

        String sql = """
                SELECT COALESCE(
                    SUM(cost),
                    0
                )
                FROM transport
                WHERE trip_id = ?
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

            ResultSet result =
                    statement.executeQuery();


            if (result.next()) {

                return result.getDouble(1);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return 0;
    }
}