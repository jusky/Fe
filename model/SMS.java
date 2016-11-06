package model;

/**
 * Created by yellowsea on 2016/7/16.
 */
public class SMS {
    private String body;
    private String address;
    private String time;
    private int date;
    private int type;
    private int recovery;

    public int getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRecovery() {
        return recovery;
    }

    public void setRecovery(int recovery) {
        this.recovery = recovery;
    }
}
