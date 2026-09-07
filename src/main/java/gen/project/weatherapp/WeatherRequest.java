package gen.project.weatherapp;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;

public class WeatherRequest {

    @NotBlank(message = "sensorId is required")
    private String sensorId;

    @NotNull(message = "time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @PastOrPresent(message = "time must be in the past or present")
    private Instant time;

    @NotNull(message = "temperature is required")
    private Float temperature;

    @NotNull(message = "humidity is required")
    private Float humidity;

    @NotNull(message = "windspeed is required")
    private Float windspeed;

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

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Float getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(Float windspeed) {
        this.windspeed = windspeed;
    }
}
