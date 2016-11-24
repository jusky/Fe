package callPic;

/**
 * Created by yellowsea on 2016/8/9.
 */
public class Edge {
    public String ConID;    //主键，区分不同的通话记录
    public String SID;      //拨出的UserID，注意这不是拨出的手机号
    public String ReID;     //接收的UserID
    public String Fre;      //一定时间内的通话频率

    public Edge(String ConID,String SID,String ReID,String Fre){
        this.ConID=ConID;
        this.SID=SID;
        this.ReID=ReID;
        this.Fre=Fre;
    }
    public String getSID(){
        return SID;
    }
    public String getReID(){
        return ReID;
    }
    public String getConID(){
        return ConID;
    }
    public String getFre(){
        return Fre;
    }

}
