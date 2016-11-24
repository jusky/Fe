package tool;

import model.Contact;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yellowsea on 2016/8/7.
 */
public class ContactsFinder {
    public static List<Contact> findContacts(String path) throws ClassNotFoundException {
        final List<Contact> contactList = new ArrayList<>();
        Class.forName("org.sqlite.JDBC");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+path);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(10);

            ResultSet rs = statement.executeQuery("SELECT number, name FROM\n" +
                    "  (SELECT raw_contact_id, data1 AS number FROM data \n" +
                    "  WHERE mimetype_id IN (SELECT _id FROM mimetypes WHERE mimetype='vnd.android.cursor.item/phone_v2'))\n" +
                    "  NATURAL JOIN \n" +
                    "  (SELECT raw_contact_id, data1 AS name FROM data \n" +
                    "  WHERE mimetype_id IN (SELECT _id FROM mimetypes WHERE mimetype='vnd.android.cursor.item/name'));");
            while (rs.next()) {
                Contact contact = new Contact();
                contact.setNumber(rs.getString("number"));
                contact.setLabel(rs.getString("name"));
                contactList.add(contact);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return contactList;
    }
}
