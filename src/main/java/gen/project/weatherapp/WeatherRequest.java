package gen.project.weatherapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.Instant;

public class WeatherRequest {

    @NotBlank(message = "sensorId is required")
    private String sensorId;

    @NotNull(message = "time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @PastOrPresent(message = "time must be in the past or present")
    private Instant time;

    @NotNull(message = "temperature is required")
    private BigDecimal temperature;

    @NotNull(message = "humidity is required")
    private BigDecimal humidity;

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getHumidity() {
        return humidity;
    }

    public void setHumidity(BigDecimal humidity) {
        this.humidity = humidity;
    }
}
