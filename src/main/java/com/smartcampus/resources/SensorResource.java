package com.smartcampus.resources;

import com.smartcampus.db.DataStore;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private DataStore dataStore = DataStore.getInstance();

    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(dataStore.getSensors().values());
        if (type == null || type.trim().isEmpty()) {
            return allSensors;
        }
        List<Sensor> filteredSensors = new ArrayList<>();
        for (Sensor sensor : allSensors) {
            if (sensor.getType() != null && type.equalsIgnoreCase(sensor.getType())) {
                filteredSensors.add(sensor);
            }
        }
        return filteredSensors;
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(sensor).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("The referenced room '" + sensor.getRoomId() + "' does not exist.");
        }
        dataStore.getSensors().put(sensor.getId(), sensor);
        
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
        
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new jakarta.ws.rs.WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new SensorReadingResource(sensorId);
    }
}
