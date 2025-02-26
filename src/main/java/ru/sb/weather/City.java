package ru.sb.weather;

/**
 * Stores information about the city.
 *
 * @author <a href="https://github.com/Berserk-Vl/">Berserk-Vl</a>
 */
public class City {
    /**
     * The city name.
     */
    private String name;
    /**
     * The latitude coordinates.
     */
    private double latitude;
    /**
     * The longitude coordinates.
     */
    private double longitude;
    /**
     * The timestamp in milliseconds indicating when weather data for this city was last updated.
     *
     * @see System#currentTimeMillis()
     */
    private long lastUpdate;
    /**
     * The string containing current weather data in this city in JSON format.
     */
    private String weather;

    /**
     * Constructs a new city object with a given name.
     *
     * @param name the city name
     */
    City(String name) {
        this.name = name;
        latitude = 0;
        longitude = 0;
        lastUpdate = 0;
        weather = "";
    }

    /**
     * Returns the city name.
     *
     * @return the city name
     */
    String getName() {
        return name;
    }

    /**
     * Returns the latitude coordinates of the city.
     *
     * @return the latitude coordinates of the city
     */
    double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude coordinates of the city.
     *
     * @param latitude the latitude coordinates of the city
     */
    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns the longitude coordinates of the city.
     *
     * @return the longitude coordinates of the city
     */
    double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude coordinates of the city.
     *
     * @param longitude the longitude coordinates of the city
     */
    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the timestamp in milliseconds indicating the
     * last time the weather data for this city was updated.
     *
     * @return the timestamp in milliseconds
     * @see System#currentTimeMillis()
     */
    long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Sets the timestamp in milliseconds indicating the
     * last time the weather data for this city was updated.
     *
     * @param lastUpdate the timestamp in milliseconds
     * @see System#currentTimeMillis()
     */
    void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * Returns the weather data for this city in JSON format.
     *
     * @return the weather data for this city in JSON format
     */
    String getWeather() {
        return weather;
    }

    /**
     * Sets the weather data for this city.
     *
     * @param weather the weather data for this city
     */
    void setWeather(String weather) {
        this.weather = weather;
    }
}
