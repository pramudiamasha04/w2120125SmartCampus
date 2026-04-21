package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Return 409 Conflict with JSON body
        String jsonBody = String.format("{\"error\": \"ROOM_NOT_EMPTY\", \"message\": \"%s\"}", exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(jsonBody)
                .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}
