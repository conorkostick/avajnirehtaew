CREATE TABLE IF NOT EXISTS weather_readings (
    id BIGSERIAL PRIMARY KEY,
    sensor_id VARCHAR(255) NOT NULL,
    time TIMESTAMP WITH TIME ZONE NOT NULL,
    temperature NUMERIC(3,1) NOT NULL,
    humidity NUMERIC(3,1) NOT NULL,
    windspeed NUMERIC(3,1) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_weather_sensor_time
    ON weather_readings (sensor_id, time);
