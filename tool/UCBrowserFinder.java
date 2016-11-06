package tool;

import model.Browser;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class UCBrowserFinder {
    public static List<Browser> findUCBrowser(String path) throws ClassNotFoundException{
        final List<Browser> browserList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM history;");
            while (rs.next()) {
                Browser browser = new Browser();
                browser.setName(rs.getString("name"));
                browser.setUrl(rs.getString("url"));
                Date date = rs.getDate("visited_time");
                Time time = rs.getTime("visited_time");
                browser.setTime(date.toString() + " " + time.toString());
                browser.setResource("UC浏览器");
                browserList.add(browser);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return browserList;
    }
}
