package callPic;

/**
 * Created by yellowsea on 2016/8/9.
 */
public class Node {
    private String UserID;         //数据库中的主键，区分不同用户
    private String UserName;       //联系人中的备注名，或者真实姓名
    private String PhoneID;        //区分不同手机
    private String Num;            //手机号

    public Node(String user_id, String user_name, String phone_id, String phone_number) {
        this.UserID=user_id;
        this.UserName=user_name;
        this.Num=phone_number;
        this.PhoneID=phone_id;
    }

    public String getUserID() {

        return UserID;
    }

    public String getUserName() {

        return UserName;
    }

    public String getPhoneID() {

        return PhoneID;
    }

    public String getNum() {
        return Num;
    }

}
