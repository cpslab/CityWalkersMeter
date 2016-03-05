package jp.ac.dendai.im.cps.citywalkersmeter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown=true)
public class LogData implements Serializable {
    private double lat;
    private double lng;
    private double accuracy;
    private double altitude;
    private double accuracy_altitude;
    private float speed;
    private float light;
    private float pressure;
    private float temprature;
    private float humidity;
    private float bearing;
    private float[] accelerometers;
    private float[] gyroscope;
    private float sound;
    private float step;
    private int is_active;
    private String myway;
    private String description;
    private int timestamp;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccuracy_altitude() {
        return accuracy_altitude;
    }

    public void setAccuracy_altitude(double accuracy_altitude) {
        this.accuracy_altitude = accuracy_altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getLight() {
        return light;
    }

    public void setLight(float light) {
        this.light = light;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getTemprature() {
        return temprature;
    }

    public void setTemprature(float temprature) {
        this.temprature = temprature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float[] getAccelerometers() {
        return accelerometers;
    }

    public void setAccelerometers(float[] accelerometers) {
        this.accelerometers = accelerometers;
    }

    public float[] getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(float[] gyroscope) {
        this.gyroscope = gyroscope;
    }

    public float getSound() {
        return sound;
    }

    public void setSound(float sound) {
        this.sound = sound;
    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public int getIs_active() {
        return is_active;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }

    public String getMyway() {
        return myway;
    }

    public void setMyway(String myway) {
        this.myway = myway;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

//    public String JSONFormat() {
//        return "{ \"lat\": " + this.latitude + ","
//                + " \"lng\":" + this.longitude + ","
//                + " \"accuracy\":" + (float)this.accuracy + ","
//                + " \"timestamp\":" + this.time + ","
//                + " \"speed\":" + this.speed + ","
//                + " \"altitude\":" + this.altitude + ","
//                + " \"bearing\":" + this.bearing
//                + "}";
//    }
}
