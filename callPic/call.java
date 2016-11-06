package callPic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by yellowsea on 2016/8/8.
 */
public class call {
    static  Connection conn;
    static Statement sql;
    static ResultSet rst;
    public String SID;
    public String ReID;
    public String ConID;
    public String Fre;
    public Connection getConnection(String path){
        try{
            Class.forName("org.sqlite.JDBC");
            conn=DriverManager.getConnection("jdbc:sqlite:" + path);
        }catch(Exception e){
            e.printStackTrace();
        }
        return conn;
    }

    public static ArrayList<Edge> transfer(String path){
        call c=new call();
        ArrayList<Edge> edgeList=new ArrayList<>();
        conn=c.getConnection(path);
        try{
            sql=conn.createStatement();
            ResultSet rs = sql.executeQuery("SELECT COUNT(*) FROM phones");
            int rows = 0;
            if (rs.next()) {
                rows = rs.getInt(1);
            }
            for (int i = 1; i <= rows; i++) {
                rst = sql.executeQuery("SELECT phones.id, COUNT(*) FROM calls, phones WHERE calls.phone_id = phones.id AND calls.number = phones.phone_number AND calls.phone_id = " + i + " AND type = 1 GROUP BY calls.number HAVING calls.number IN (SELECT phone_number FROM phones);");
                while (rst.next()) {
                    int j = 1;
                    String SID = String.valueOf(i);
                    String ReID = rst.getString(1);
                    String ConID = String.valueOf(j++);
                    String Fre = rst.getString(2);
                    Edge edge = new Edge(ConID, SID, ReID, Fre);
                    edgeList.add(edge);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return edgeList;
        }
    }


}
