package gen.project.weatherapp;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<WeatherReading, Long> {

    List<WeatherReading> findBySensorIdInOrderByTimeAsc(List<String> sensorIds);

    List<WeatherReading> findBySensorIdInAndTimeBetweenOrderByTimeAsc(
        List<String> sensorIds, Instant startTime, Instant endTime);

    List<WeatherReading> findBySensorIdInAndTimeGreaterThanEqualOrderByTimeAsc(
        List<String> sensorIds, Instant startTime);

    List<WeatherReading> findBySensorIdInAndTimeLessThanEqualOrderByTimeAsc(
        List<String> sensorIds, Instant endTime);

    List<WeatherReading> findAllByOrderByTimeAsc();

    List<WeatherReading> findAllByOrderByTimeDesc();

    List<WeatherReading> findBySensorIdInOrderByTimeDesc(List<String> sensorIds);
}
