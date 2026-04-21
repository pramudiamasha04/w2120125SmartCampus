package com.smartcampus.resources;

import com.smartcampus.db.DataStore;
import com.smartcampus.models.Room;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private DataStore dataStore = DataStore.getInstance();

    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.getRooms().values());
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(room).build();
    }

    @POST
    public Response createRoom(Room room) {
        dataStore.getRooms().put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new com.smartcampus.exceptions.RoomNotEmptyException("Cannot delete room " + roomId + " — it still has active sensors assigned.");
        }
        dataStore.getRooms().remove(roomId);
        return Response.noContent().build();
    }
}
