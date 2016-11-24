package tool;

import model.JDSearchHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yellowsea on 2016/8/10.
 */
public class JDSearchHistoryFinder {
    public static List<JDSearchHistory> findJDSearchHistory(String path) {
        final List<JDSearchHistory> histories = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM search_history;");
            while (rs.next()) {
                JDSearchHistory history = new JDSearchHistory();
                history.setWord(rs.getString("word"));
                Date date = rs.getDate("search_time");
                Time time = rs.getTime("search_time");
                history.setSearchTime(date.toString()+" "+time.toString());
                histories.add(history);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return histories;
    }
}
