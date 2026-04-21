package com.smartcampus.resources;

import com.smartcampus.db.DataStore;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        if (sensor.getRoomId() == null) {
            throw new LinkedResourceNotFoundException("The referenced room 'null' does not exist.");
        }
        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("The referenced room '" + sensor.getRoomId() + "' does not exist.");
        }
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            sensor.setId(java.util.UUID.randomUUID().toString());
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
            throw new javax.ws.rs.WebApplicationException(Response.Status.NOT_FOUND);
        }
        return new SensorReadingResource(sensorId);
    }
}
