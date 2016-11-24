package tool;

import model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class HotelFinder {
    public static List<Hotel> findHotel(String path) throws ClassNotFoundException {
        final List<Hotel> hotelList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM city_query_history;");
            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setCityName(rs.getString("cityName"));
                hotel.setPositionCoordinates(rs.getString("airportcode"));
                hotel.setPositionName(rs.getString("airportname"));
                hotel.setTime(rs.getString("date"));
                hotelList.add(hotel);
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
        return hotelList;
    }
}
