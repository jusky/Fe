package tool;

import model.Browser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class QQBrowserFinder {
    public static List<Browser> findQQBrowser(String path) throws ClassNotFoundException {
        final List<Browser> browserList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM history;");
            while (rs.next()) {
                Browser browser = new Browser();
                browser.setName(rs.getString("name"));
                browser.setUrl(rs.getString("url"));
                Date date = rs.getDate("datetime");
                Time time = rs.getTime("datetime");
                browser.setTime(date.toString()+" "+time.toString());
                browser.setResource("QQ浏览器");
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
                System.err.println(e.getMessage());
            }
        }
        return browserList;
    }
}
