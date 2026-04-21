package com.smartcampus.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Return 409 Conflict with JSON body
        String jsonBody = String.format("{\"error\": \"ROOM_NOT_EMPTY\", \"message\": \"%s\"}", exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(jsonBody)
                .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}
