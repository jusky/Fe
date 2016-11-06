package tool;

import model.Flight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class FlightFinder {
    public static List<Flight> findFlight(String path) throws ClassNotFoundException {
        final List<Flight> flightList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+path);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM city_query_history;");
            while (rs.next()) {
                Flight flight = new Flight();
                flight.setCityName(rs.getString("cityName"));
                flight.setTime(rs.getString("date"));
                flightList.add(flight);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        return flightList;
    }
}
