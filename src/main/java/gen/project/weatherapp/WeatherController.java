package gen.project.weatherapp;

import jakarta.validation.Valid;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// make temperature and humidity enums? - done
// also same for avg, max and min - done
// change Big Decimal or change DB schema to take in appropriate values - done
// implement windspeed - done

// Need to make POST fields manditory
// Need to create input validation
// Need to handle error messages better
// Need to manage API responses better

// limitations: only one stat or metric per get request
//                post requests must have all values 

@RestController
@RequestMapping("/api/v1")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

List<String> metrics = new ArrayList<>(
    List.of("temperature", "humidity", "windspeed")
);

List<String> stats = new ArrayList<>(
    List.of("min", "max", "avg")
);


    @PostMapping("/weather")
    public ResponseEntity<String> createWeatherReading(@Valid @RequestBody WeatherRequest request) {
        if (request.getSensorId() == null || request.getTime() == null || request.getTemperature() == null ||
            request.getHumidity() == null || request.getWindspeed() == null) {
                throw new IllegalArgumentException("Missing vital parameter, weather not logged");
            }
        WeatherReading saved = weatherService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Weather data saved successfully");
    }

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather(
            @RequestParam List<String> sensorId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String metric,
            @RequestParam(defaultValue = "avg") String stat) {

        Instant startInstant = parseInstant(startTime);
        Instant endInstant = parseInstant(endTime);
        String validatedMetric = validateMetric(metric);
        String validatedStat = validateStat(stat);

        List<WeatherReading> readings = weatherService.getWeather(
                sensorId, startInstant, endInstant, validatedMetric, validatedStat);
        if (readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Time parameters must be valid ISO-8601 timestamps");
        }
    }

    private String validateMetric(String metric) {
        if (metric == null || metric.isBlank()) {
            return null;
        }
        String validated = metric.toLowerCase(Locale.ROOT);
        if (!metrics.contains(validated)) {
            throw new IllegalArgumentException("metric must be temperature, humidity or windspeed");
        }
        return validated;
    }

    private String validateStat(String stat) {
        String validated = stat.toLowerCase(Locale.ROOT);
        if (!stats.contains(validated)) {
            throw new IllegalArgumentException("stat must be min, max, or avg");
        }
        return validated;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
