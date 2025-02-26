WeatherSDK
===============================

# Overview

It is a small software developer kit(SDK) that can be used by other developers to easily access a [weather API](https://openweathermap.org/current) and retrieve weather data for a given location.

# Build Instructions

The WeatherSDK can be built from the command line, [Maven](https://maven.apache.org/download.cgi), IDE.

It depends on JSON-Java([maven](https://mvnrepository.com/artifact/org.json/json), [git](https://github.com/stleary/JSON-java/)),
so in order to compile from the command line or use this SDK you need to download **json.jar** from the Maven or git.

And you need an **apiKey** for [openweathermap](https://home.openweathermap.org/users/sign_up), which you can get after free registration (the key for the basic version is also free).

During compilation or packaging, you must specify a relative or absolute path to the .jar files.

In the examples, the .jar files are located in the same directory in which the commands are executed.

**Building from the command line**

*Build the class files from the package root directory src/main/java*
```shell
javac -cp .;json.jar ru/sb/weather/*.java
```

*Create the .jar file in the current directory*
```shell
jar cf WeatherSDK.jar ru/sb/weather/*.class
```

*Compile a program that uses the .jar (see example code below)*
```shell
javac -cp .;WeatherSDK.jar;json.jar Main.java (Windows)
javac -cp .:WeatherSDK.jar:json.jar Main.java (Unix Systems)
```

**Building .jar using [Maven](https://maven.apache.org/download.cgi)**

*Run this command from the directory where pom.xml is located, the created .jar will be in the target(created by mvn) directory.*
```shell
mvn package
```

*Main file contents*

```java
import ru.sb.weather.*;
public class Main {
    public static void main(String []args){
        String apiKey = "<your apiKey>";
        Weather weather = WeatherManager.getWeather(apiKey);
        //OR
        // Weather weather = WeatherManager.getWeather(apiKey, Mode.POLLING);
        System.out.println(weather.getWeatherFor("London"));
    }
}
```

*Execute the Main file*
```shell 
java -cp .;WeatherSDK.jar;json.jar Main (Windows)
java -cp .:WeatherSDK.jar:json.jar Main (Unix Systems)
```

*Expected output*

```json
{"datetime":1740603910,"visibility":10000,"timezone":0,"weather":{"description":"broken clouds","main":"Clouds"},"temperature":{"temp":278.49,"feels_like":274.62},"name":"London","sys":{"sunrise":1740552732,"sunset":1740591297},"wind":{"speed":5.66}}
```
