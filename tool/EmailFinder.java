package tool;

import model.Call;
import model.Email;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class EmailFinder {
    public static List<Email> findEmail(String path) throws ClassNotFoundException {
        final List<Email> emailList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM message;");
            while (rs.next()) {
                Email email = new Email();
                email.setSubject(rs.getString("subject"));
                email.setFromList(rs.getString("fromList"));
                email.setToList(rs.getString("toList"));
                Date date = rs.getDate("timeStamp");
                Time time = rs.getTime("timeStamp");
                email.setTime(date.toString()+" "+time.toString());
                emailList.add(email);
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
        return emailList;
    }
}
