package ru.sb.weather;

import java.util.HashMap;
import java.util.Map;

public class WeatherManager {
    private static Map<String, Weather> weatherMap;

    private WeatherManager(){}

    public static synchronized Weather getWeather(String apiKey) {
        return getWeather(apiKey, Mode.ON_DEMAND);
    }

    public static synchronized Weather getWeather(String apiKey, Mode mode) {
        if (apiKey == null || apiKey.length() == 0) {
            throw new IllegalArgumentException("APIKey must not be null or have length 0.");
        }
        if (mode == null) {
            throw new IllegalArgumentException("Mode must not be null.");
        }
        if (weatherMap == null) {
            weatherMap = new HashMap<>();
        }
        if (!weatherMap.containsKey(apiKey) || !weatherMap.get(apiKey).isActive()) {
            weatherMap.put(apiKey, new Weather(apiKey, mode));
        }
        return weatherMap.get(apiKey);
    }

    public static synchronized boolean deleteWeather(Weather weather) {
        if (weather != null && weatherMap.containsKey(weather.getApiKey())) {
            weatherMap.remove(weather.getApiKey());
            weather.deactivate();
            return true;
        }
        return false;
    }
}
