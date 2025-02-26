package ru.sb.weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages Weather objects.
 * Responsible for the creation, storage, and deletion of created objects.
 * This is a thread-safe implementation.
 *
 * @author <a href="https://github.com/Berserk-Vl/">Berserk-Vl</a>
 */
public class WeatherManager {
    /**
     * The storage for created Weather objects.
     */
    private static Map<String, Weather> weatherMap;

    /**
     * The default constructor is hidden.
     */
    private WeatherManager(){}

    /**
     * Returns the <code>Weather</code> object that uses/will use the specified apiKey.
     * The mode for a new <code>Weather</code> object by default is set to <code>Mode.ON_DEMAND</code>.
     *
     * @param apiKey the apiKey that <code>Weather</code> object uses/will use
     * @return the <code>Weather</code> object that uses/will use the specified apiKey
     * @throws IllegalArgumentException if the apiKey is null or its length is zero
     * @see Mode
     */
    public static synchronized Weather getWeather(String apiKey) {
        return getWeather(apiKey, Mode.ON_DEMAND);
    }

    /**
     * Returns the <code>Weather</code> object that uses/will use the specified apiKey.
     * If for the specified apiKey the <code>Weather</code> object does not exist a new one
     * will be created and returned.
     * If for the specified apiKey the <code>Weather</code> object already exists
     * this object will be returned; if this object is inactive, a new one will be
     * created with the specified parameters.
     *
     * @param apiKey the apiKey that Weather object uses/will use
     * @param mode   the mode in which the created new <code>Weather</code> object will work
     * @return the <code>Weather</code> object that uses/will use the specified apiKey
     * @throws IllegalArgumentException if the apiKey or mode is null or apiKey length is zero
     * @see Mode
     */
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

    /**
     * Deactivates and deletes from the storage specified <code>Weather</code> object.
     *
     * @param weather the object to be deleted
     * @return true if the object was deleted, false otherwise
     */
    public static synchronized boolean deleteWeather(Weather weather) {
        if (weather != null && weatherMap.containsKey(weather.getApiKey())) {
            weatherMap.remove(weather.getApiKey());
            weather.deactivate();
            return true;
        }
        return false;
    }
}
