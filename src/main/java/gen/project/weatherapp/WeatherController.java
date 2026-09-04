package gen.project.weatherapp;

import jakarta.validation.Valid;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping("/weather")
    public ResponseEntity<WeatherReading> createWeatherReading(@Valid @RequestBody WeatherRequest request) {
        WeatherReading saved = weatherService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather(
            @RequestParam(required = false) List<String> sensorId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String metric,
            @RequestParam(defaultValue = "avg") String stat) {

        Instant startInstant = parseInstant(startTime);
        Instant endInstant = parseInstant(endTime);
        String normalizedMetric = normalizeMetric(metric);
        String normalizedStat = normalizeStat(stat);

        List<WeatherReading> readings = weatherService.getWeather(
                sensorId, startInstant, endInstant, normalizedMetric, normalizedStat);
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

    private String normalizeMetric(String metric) {
        if (metric == null || metric.isBlank()) {
            return null;
        }
        String normalized = metric.toLowerCase(Locale.ROOT);
        if (!normalized.equals("temperature") && !normalized.equals("humidity")) {
            throw new IllegalArgumentException("metric must be temperature or humidity");
        }
        return normalized;
    }

    private String normalizeStat(String stat) {
        String normalized = stat.toLowerCase(Locale.ROOT);
        if (!normalized.equals("min") && !normalized.equals("max") && !normalized.equals("avg")) {
            throw new IllegalArgumentException("stat must be min, max, or avg");
        }
        return normalized;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().build();
    }
}
