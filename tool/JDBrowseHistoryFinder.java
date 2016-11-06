package tool;

import model.JDBrowseHistory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yellowsea on 2016/8/10.
 */
public class JDBrowseHistoryFinder {
    public static List<JDBrowseHistory> findJDBroseHistory(String path) {
        final List<JDBrowseHistory> histories = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT * FROM BrowseHistoryTable;");
            while (rs.next()) {
                JDBrowseHistory history = new JDBrowseHistory();
                history.setProductCode(rs.getString("productCode"));
                history.setBrowseTime(rs.getString("browseTime"));
                histories.add(history);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return histories;
    }
}
