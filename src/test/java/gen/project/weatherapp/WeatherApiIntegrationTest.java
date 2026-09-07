package gen.project.weatherapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateWeatherReadingAndReturnLatestForSensor() throws Exception {
        String payload = """
                {
                  "sensorId": "sensor-1",
                  "time": "2026-09-03T15:00:00Z",
                  "temperature": 21.5,
                  "humidity": 55.0,
                  "windspeed": 13.3
                }
                """;

        mockMvc.perform(post("/api/v1/weather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/weather")
                        .param("sensorId", "sensor-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value("sensor-1"))
                .andExpect(jsonPath("$[0].temperature").value(21.5))
                .andExpect(jsonPath("$[0].humidity").value(55.0))
                .andExpect(jsonPath("$[0].windspeed").value(13.3));
    }

    @Test
    void shouldReturnWeatherReadingsWithinTimeRange() throws Exception {
        mockMvc.perform(post("/api/v1/weather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sensorId": "sensor-2",
                                  "time": "2026-09-03T10:00:00Z",
                                  "temperature": 18.0,
                                  "humidity": 60.0,
                                  "windspeed": 18.2
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/weather")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sensorId": "sensor-2",
                                  "time": "2026-09-03T12:00:00Z",
                                  "temperature": 20.0,
                                  "humidity": 65.0,
                                  "windspeed": 4.7
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/weather")
                        .param("sensorId", "sensor-2")
                        .param("startTime", "2026-09-03T09:00:00Z")
                        .param("endTime", "2026-09-03T11:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorId").value("sensor-2"))
                .andExpect(jsonPath("$[0].temperature").value(18.0))
                .andExpect(jsonPath("$[0].humidity").value(60.0))
                .andExpect(jsonPath("$[0].windspeed").value(18.2));

                }

                @Test
                void shouldAggregateRepeatedSensorIdsAndSelectMetric() throws Exception {
                                createReading("sensor-3", "2026-09-03T10:00:00Z", 10.0f, 40.0f, 12.0f);
                                createReading("sensor-3", "2026-09-03T11:00:00Z", 20.0f, 60.0f, 17.0f);
                                createReading("sensor-4", "2026-09-03T10:00:00Z", 30.0f, 70.0f, 3.0f);

                                mockMvc.perform(get("/api/v1/weather")
                                .param("sensorId", "sensor-3", "sensor-4")
                                .param("startTime", "2026-09-03T09:00:00Z")
                                .param("endTime", "2026-09-03T12:00:00Z")
                                .param("metric", "temperature")
                                .param("stat", "max"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[0].sensorId").value("sensor-3"))
                                        .andExpect(jsonPath("$[0].temperature").value(20.0))
                                        .andExpect(jsonPath("$[0].humidity").doesNotExist())
                                        .andExpect(jsonPath("$[0].windspeed").doesNotExist())
                                        .andExpect(jsonPath("$[1].sensorId").value("sensor-4"))
                                        .andExpect(jsonPath("$[1].temperature").value(30.0));
                }

                @Test
                void shouldUseAverageByDefaultForAllSensors() throws Exception {
                                createReading("sensor-5", "2026-09-03T10:00:00Z", 10.0f, 40.0f, 10.0f);
                                createReading("sensor-5", "2026-09-03T11:00:00Z", 20.0f, 60.0f, 5.0f);

                                mockMvc.perform(get("/api/v1/weather")
                                .param("startTime", "2026-09-03T09:00:00Z")
                                .param("endTime", "2026-09-03T12:00:00Z"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[?(@.sensorId == 'sensor-5')].temperature").value(15.0))
                                        .andExpect(jsonPath("$[?(@.sensorId == 'sensor-5')].humidity").value(50.0))
                                        .andExpect(jsonPath("$[?(@.sensorId == 'sensor-5')].windspeed").value(7.5));
                                                }

                private void createReading(String sensorId, String time, Float temperature, Float humidity, Float windspeed) throws Exception {
                                mockMvc.perform(post("/api/v1/weather")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(       """
                                                {
                                                        "sensorId": "%s",
                                                        "time": "%s",
                                                        "temperature": %.1f,
                                                        "humidity": %.1f,
                                                        "windspeed": %.1f
                                                }
                                                """.formatted(sensorId, time, temperature, humidity)))
                                .andExpect(status().isCreated());
    }
}
