package ru.sb.weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Weather {
    private static long CACHE_DURATION = 595000;
    private String apiKey;
    private boolean active;
    private boolean polling;
    private Mode mode;
    private Map<String, City> cityMap;
    private Queue<City> cityQueue;

    Weather(String apiKey, Mode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        active = true;
        polling = false;
        cityMap = new HashMap<>();
        cityQueue = new LinkedList<>();
    }

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

    public String getApiKey() {
        return apiKey;
    }

    public boolean isActive() {
        return active;
    }

    void deactivate() {
        active = false;
    }

    private void updateCityWeather(City city) {
        if (System.currentTimeMillis() - city.getLastUpdate() >= CACHE_DURATION) {
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + city.getLatitude() + "&lon=" + city.getLongitude() + "&appid=" + apiKey;

            city.setWeather(parseWeather(getRequest(url)));
            city.setLastUpdate(System.currentTimeMillis());
        }
    }

    private synchronized void updateCitiesWeather() {
        for (City city : cityQueue) {
            updateCityWeather(city);
        }
    }

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

    private void setCityCoordinates(City city) {
        String url = "https://api.openweathermap.org/geo/1.0/direct?q=" + city.getName() + "&limit=1&appid=" + apiKey;

        double[] coordinates = parseCityCoordinates(getRequest(url));
        city.setLatitude(coordinates[0]);
        city.setLongitude(coordinates[1]);
    }

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
