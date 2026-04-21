package com.smartcampus.resources;

import com.smartcampus.db.DataStore;
import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SensorReadingResource {

    private String sensorId;
    private DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<SensorReading> getReadings() {
        return dataStore.getSensorReadings().getOrDefault(sensorId, new ArrayList<>());
    }

    @POST
    public Response createReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId + "' is in MAINTENANCE mode and cannot accept readings.");
        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        List<SensorReading> readings = dataStore.getSensorReadings().computeIfAbsent(sensorId, k -> new ArrayList<>());
        readings.add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
