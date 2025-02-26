package ru.sb.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Provides weather information for specified city/cities.
 * This is a thread-safe implementation.
 *
 * @author <a href="https://github.com/Berserk-Vl/">Berserk-Vl</a>
 */
public class Weather {
    /**
     * The duration in milliseconds during which weather data will
     * be considered fresh and not requiring updating.
     */
    private static long CACHE_DURATION = 595000;
    /**
     * The apiKey that used to request weather from the server.
     */
    private String apiKey;
    /**
     * A state indicating whether this object can be used for weather queries.
     */
    private boolean active;
    /**
     * A state indicating whether automatic weather data update mode is running.
     */
    private boolean polling;
    /**
     * The mode in which this object works.
     *
     * @see Mode
     */
    private Mode mode;
    /**
     * Storage for data about cities whose weather has already been requested.
     */
    private Map<String, City> cityMap;
    /**
     * The order in which cities were added to the storage.
     */
    private Queue<City> cityQueue;

    /**
     * Creates a new Weather object with specified apiKey and mode.
     *
     * @param apiKey the apiKey that will be used to request weather from the server
     * @param mode   the mode in which this object will operate
     * @see Mode
     */
    Weather(String apiKey, Mode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        active = true;
        polling = false;
        cityMap = new HashMap<>();
        cityQueue = new LinkedList<>();
    }

    /**
     * Returns a weather data in the requested city.
     *
     * @param cityName the name of the city where need to find out the weather
     * @return the weather data in JSON format
     * @throws IllegalArgumentException if the cityName is null or its length is 0
     * @throws RuntimeException         indicates that something went wrong while receiving weather data,
     *                                  the exception will contain a message stating what exactly went wrong
     */
    public synchronized String getWeatherFor(String cityName) {
        if (!active) {
            throw new RuntimeException("Trying to get weather from an object that has been removed from WeatherManager and/or marked as inactive.");
        }
        if (cityName == null || cityName.length() == 0) {
            throw new IllegalArgumentException("City name must not be null or have length 0.");
        }
        if (!cityMap.containsKey(cityName)) {
            initializeCity(cityName);
        }
        updateCityWeather(cityMap.get(cityName));
        if (mode == Mode.POLLING && !polling) {
            startPollingMode();
        }
        return cityMap.get(cityName).getWeather();
    }

    /**
     * Return the apiKey used in this object.
     *
     * @return the apiKey used in this object
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Shows whether this object is active or not,
     * that is whether this object can be used for weather queries in cities.
     *
     * @return true if this object can be used to get weather data, otherwise - false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Marks this object as not active.
     */
    void deactivate() {
        active = false;
    }

    /**
     * Updates weather data for the specified city.
     *
     * @param city the city whose weather data needs to be updated
     */
    private void updateCityWeather(City city) {
        if (System.currentTimeMillis() - city.getLastUpdate() >= CACHE_DURATION) {
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + city.getLatitude() + "&lon=" + city.getLongitude() + "&appid=" + apiKey;

            city.setWeather(parseWeather(getRequest(url)));
            city.setLastUpdate(System.currentTimeMillis());
        }
    }

    /**
     * Updates weather data for all cities that is currently stored in this object.
     */
    private synchronized void updateCitiesWeather() {
        for (City city : cityQueue) {
            updateCityWeather(city);
        }
    }

    /**
     * Creates and initialize an object for the given city name and adds it to the storage.
     *
     * @param cityName the name of the city that is being initialized
     */
    private void initializeCity(String cityName) {
        City city = new City(cityName);
        setCityCoordinates(city);

        if (cityQueue.size() == 10) {
            cityMap.remove(cityQueue.peek().getName());
            cityQueue.poll();
        }
        cityQueue.add(city);
        cityMap.put(city.getName(), city);
    }

    /**
     * Request from the server and sets the latitude and longitude for the specified city.
     *
     * @param city the city whose coordinates need to be initialized
     */
    private void setCityCoordinates(City city) {
        String url = "https://api.openweathermap.org/geo/1.0/direct?q=" + city.getName() + "&limit=1&appid=" + apiKey;

        double[] coordinates = parseCityCoordinates(getRequest(url));
        city.setLatitude(coordinates[0]);
        city.setLongitude(coordinates[1]);
    }

    /**
     * Parses the JSON string containing weather data received from a server into another JSON string,
     * but with the desired structure and content.
     *
     * @param json the JSON string containing weather information
     * @return the JSON string in desired structure
     */
    private String parseWeather(String json) {
        JSONObject weatherData = new JSONObject();
        try {
            JSONObject rawObject = new JSONObject(json);
            JSONObject weatherObject = rawObject.getJSONArray("weather").getJSONObject(0);
            JSONObject mainObject = rawObject.getJSONObject("main");
            JSONObject windObject = rawObject.getJSONObject("wind");
            JSONObject sysObject = rawObject.getJSONObject("sys");

            weatherData.put("weather", new JSONObject());
            weatherData.getJSONObject("weather").put("main", weatherObject.getString("main"));
            weatherData.getJSONObject("weather").put("description", weatherObject.getString("description"));

            weatherData.put("temperature", new JSONObject());
            weatherData.getJSONObject("temperature").put("temp", mainObject.getDouble("temp"));
            weatherData.getJSONObject("temperature").put("feels_like", mainObject.getDouble("feels_like"));

            weatherData.put("visibility", rawObject.getInt("visibility"));

            weatherData.put("wind", new JSONObject());
            weatherData.getJSONObject("wind").put("speed", windObject.getDouble("speed"));

            weatherData.put("datetime", rawObject.getLong("dt"));

            weatherData.put("sys", new JSONObject());
            weatherData.getJSONObject("sys").put("sunrise", sysObject.getLong("sunrise"));
            weatherData.getJSONObject("sys").put("sunset", sysObject.getLong("sunset"));

            weatherData.put("timezone", rawObject.getInt("timezone"));
            weatherData.put("name", rawObject.getString("name"));
        } catch (JSONException e) {
            throw new JSONException("The string that is supposed to contain weather data does not match the expected format: " + json, e);
        }
        return weatherData.toString();
    }

    /**
     * Parses the JSON string containing city coordinates and returns result as an array.
     *
     * @param json the JSON string with city coordinates
     * @return array that contains the latitude and longitude(in this order) of the city.
     */
    private double[] parseCityCoordinates(String json) {
        double[] coordinates = new double[2];
        try {
            JSONObject jsonObject = new JSONArray(json).getJSONObject(0);
            coordinates[0] = jsonObject.getDouble("lat");
            coordinates[1] = jsonObject.getDouble("lon");
        } catch (JSONException e) {
            throw new JSONException("The string that should contain the city coordinates does not match the expected format: " + json, e);
        }
        return coordinates;
    }

    /**
     * Creates and starts a thread that will automatically query the weather for the cities stored in this object's storage.
     */
    private void startPollingMode() {
        Thread thread = new Thread(() -> {
            polling = true;
            while (active) {
                try {
                    updateCitiesWeather();
                } catch (RuntimeException e) {
                    active = false;
                    throw new RuntimeException("Failed to automatically update weather data.", e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    active = false;
                    throw new RuntimeException("A thread that automatically updates weather data was unexpectedly interrupted.", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sends GET request to the server.
     *
     * @param url the address to which the request will be made
     * @return the server response
     */
    private String getRequest(String url) {
        StringBuilder sb = new StringBuilder();
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Server returned HTTP response code and message: " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("GET request for URL: " + url + " failed.", e);
        }

        return sb.toString();
    }
}
