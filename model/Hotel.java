package model;

/**
 * Created by yellowsea on 2016/7/20.
 */
public class Hotel {
    private String cityName;
    private String positionCoordinates;
    private String positionName;
    private String time;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPositionCoordinates() {
        return positionCoordinates;
    }

    public void setPositionCoordinates(String positionCoordinates) {
        this.positionCoordinates = positionCoordinates;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
