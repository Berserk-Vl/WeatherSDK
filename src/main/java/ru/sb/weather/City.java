package ru.sb.weather;

public class City {
    private String name;
    private double latitude;
    private double longitude;
    private long lastUpdate;
    private String weather;

    City(String name) {
        this.name = name;
        latitude = 0;
        longitude = 0;
        lastUpdate = 0;
        weather = "";
    }

    String getName() {
        return name;
    }

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    long getLastUpdate() {
        return lastUpdate;
    }

    void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    String getWeather() {
        return weather;
    }

    void setWeather(String weather) {
        this.weather = weather;
    }
}
