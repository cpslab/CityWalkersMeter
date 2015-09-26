package jp.ac.dendai.im.cps.citywalkersmeter;

/**
 * Created by naoya on 2015/08/19.
 */
public class LogData {
    private double latitude;
    private double longitude;
    private float accuracy;
    private int time;
    private float speed;
    private  double altitude;
    private float bearing;

    public static LogData newInstance(double latitude, double longitude, float accuracy, int time, float speed, double altitude, float bearing) {
        LogData data = new LogData();
        data.setLatitude(latitude);
        data.setLongitude(longitude);
        data.setAccuracy(accuracy);
        data.setTime(time);
        data.setSpeed(speed);
        data.setAltitude(altitude);
        data.setBearing(bearing);
        return data;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public String JSONFormat() {
        return "{ \"lat\": " + this.latitude + ","
                + " \"lng\":" + this.longitude + ","
                + " \"accuracy\":" + (float)this.accuracy + ","
                + " \"timestamp\":" + this.time + ","
                + " \"speed\":" + this.speed + ","
                + " \"altitude\":" + this.altitude + ","
                + " \"bearing\":" + this.bearing
                + "}";
    }
}
