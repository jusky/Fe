package tool;

import model.Call;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/16.
 */
public class CallsFinder {
    public static List<Call> findCalls(String path) throws ClassNotFoundException {
        final List<Call> callList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM calls;");
            while (rs.next()) {
                Call call = new Call();
                call.setNumber(rs.getString("number"));
                call.setDuration(rs.getInt("duration"));
                call.setType(rs.getInt("type"));
                //call.setNumberLabel(rs.getString("numberlabel"));
                Date date = rs.getDate("date");
                Time time = rs.getTime("date");
                call.setTime(date.toString()+" "+time.toString());
                callList.add(call);
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
        return callList;
    }
}
