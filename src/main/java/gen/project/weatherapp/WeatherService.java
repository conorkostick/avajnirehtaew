package gen.project.weatherapp;

import java.time.Instant;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;

    public WeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public WeatherReading save(WeatherRequest request) {
        WeatherReading reading = new WeatherReading(
                request.getSensorId(),
                request.getTime(),
                request.getTemperature(),
                request.getHumidity(),
                request.getWindspeed()
        );
        return weatherRepository.save(reading);
    }

    public List<WeatherReading> getWeather(
            List<String> sensorIds, Instant startTime, Instant endTime, String metric, String stat) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must be before or equal to endTime");
        }

        List<WeatherReading> readings;
        if (startTime != null && endTime != null) {
            readings = findBetween(sensorIds, startTime, endTime);
        } else if (startTime != null) {
            readings = findFrom(sensorIds, startTime);
        } else if (endTime != null) {
            readings = findUntil(sensorIds, endTime);
        } else {
            return latestBySensor(sensorIds, metric);
        }

        return aggregateBySensor(readings, metric, stat);
    }

    private List<WeatherReading> findBetween(List<String> sensorIds, Instant startTime, Instant endTime) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            return weatherRepository.findAllByOrderByTimeAsc().stream()
                    .filter(reading -> !reading.getTime().isBefore(startTime) && !reading.getTime().isAfter(endTime))
                    .toList();
        }
        return weatherRepository.findBySensorIdInAndTimeBetweenOrderByTimeAsc(sensorIds, startTime, endTime);
    }

    private List<WeatherReading> findFrom(List<String> sensorIds, Instant startTime) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            return weatherRepository.findAllByOrderByTimeAsc().stream()
                    .filter(reading -> !reading.getTime().isBefore(startTime)).toList();
        }
        return weatherRepository.findBySensorIdInAndTimeGreaterThanEqualOrderByTimeAsc(sensorIds, startTime);
    }

    private List<WeatherReading> findUntil(List<String> sensorIds, Instant endTime) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            return weatherRepository.findAllByOrderByTimeAsc().stream()
                    .filter(reading -> !reading.getTime().isAfter(endTime)).toList();
        }
        return weatherRepository.findBySensorIdInAndTimeLessThanEqualOrderByTimeAsc(sensorIds, endTime);
    }

    private List<WeatherReading> latestBySensor(List<String> sensorIds, String metric) {
        List<WeatherReading> readings = sensorIds == null || sensorIds.isEmpty()
                ? weatherRepository.findAllByOrderByTimeDesc()
                : weatherRepository.findBySensorIdInOrderByTimeDesc(sensorIds);
        Map<String, WeatherReading> latest = new LinkedHashMap<>();
        for (WeatherReading reading : readings) {
            latest.putIfAbsent(reading.getSensorId(), selectMetric(reading, metric));
        }
        return new ArrayList<>(latest.values());
    }

    private List<WeatherReading> aggregateBySensor(List<WeatherReading> readings, String metric, String stat) {
        return readings.stream().collect(Collectors.groupingBy(
                        WeatherReading::getSensorId, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> aggregate(entry.getKey(), entry.getValue(), metric, stat))
                .toList();
    }

    private WeatherReading aggregate(String sensorId, List<WeatherReading> readings, String metric, String stat) {
        Float temperature = metric == null || metric.equals("temperature")
                ? calculate(readings, WeatherReading::getTemperature, stat) : null;
        Float humidity = metric == null || metric.equals("humidity")
                ? calculate(readings, WeatherReading::getHumidity, stat) : null;
        Float windspeed = metric == null || metric.equals("windspeed")
                ? calculate(readings, WeatherReading::getWindspeed, stat) : null;
        return new WeatherReading(sensorId, null, temperature, humidity, windspeed);
    }

private Float calculate(List<WeatherReading> readings,
        Function<WeatherReading, Float> value,
        String stat) {

    if ("min".equals(stat)) {
        return readings.stream()
                .map(value)
                .min(Float::compare)
                .orElse(null);
    }

    if ("max".equals(stat)) {
        return readings.stream()
                .map(value)
                .max(Float::compare)
                .orElse(null);
    }

    if (readings.isEmpty()) {
        return null;
    }

    Float sum = readings.stream()
            .map(value)
            .reduce(0f, Float::sum);

    return Math.round((sum / readings.size()) * 100f) / 100f;
}


    private WeatherReading selectMetric(WeatherReading reading, String metric) {
        if (metric == null) {
            return reading;
        }
        return new WeatherReading(reading.getSensorId(), reading.getTime(),
                metric.equals("temperature") ? reading.getTemperature() : null,
                metric.equals("humidity") ? reading.getHumidity() : null,
                metric.equals("windspeed") ? reading.getWindspeed() : null);
    }
}
