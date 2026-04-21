package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        String jsonBody = String.format("{\"error\": \"SENSOR_UNAVAILABLE\", \"message\": \"%s\"}", exception.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(jsonBody)
                .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}
