package callPic;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by yellowsea on 2016/8/9.
 */
public class Call_Record {
    static Connection dbConn;
    static Statement sql;
    static ResultSet rst;
    public String UserID;
    public String UserName;
    public String PhoneID;
    public String Num;
    public Connection getConnection(String path){

        try{
            Class.forName("org.sqlite.JDBC");
            dbConn=DriverManager.getConnection("jdbc:sqlite:" + path);
        }catch(Exception e){
            e.printStackTrace();
        }
        return dbConn;
    }

    public static ArrayList<Node> transfer(String path) {
        Call_Record c=new Call_Record();
        ArrayList<Node> nodeList = new ArrayList<>();
        dbConn=c.getConnection(path);
        try{
            sql=dbConn.createStatement();//实例化Statement对象
            //执行SQL语句，返回结果集
            rst=sql.executeQuery("SELECT * FROM phones;");
            while(rst.next()){
                String UserID=rst.getString("id");//获取列名是“user_id”的字段值
                String UserName=rst.getString("owner_name");//获取列名是“user_name”的字段值
                String PhoneID=rst.getString("id");//获取列名是“phone_id”的字段值
                String Num=rst.getString("phone_number");//获取列名是“phone_number”的字段值
                Node node = new Node(UserID, UserName, PhoneID, Num);
                nodeList.add(node);
            }
        }catch(Exception e){
            e.printStackTrace();
        } finally {
            return nodeList;
        }
    }
}
