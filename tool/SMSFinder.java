package tool;

import model.SMS;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class SMSFinder {
    public static List<SMS> findSMS(String path) throws ClassNotFoundException {
        final List<SMS> smsList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM sms;");
            while (rs.next()) {
                SMS sms = new SMS();
                sms.setAddress(rs.getString("address"));
                sms.setBody(rs.getString("body"));
                Date date = rs.getDate("date");
                Time time = rs.getTime("date");
                sms.setDate(rs.getInt("date"));
                sms.setTime(date.toString()+" "+time.toString());
                sms.setType(rs.getInt("type"));
                smsList.add(sms);
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
        return smsList;
    }
}
