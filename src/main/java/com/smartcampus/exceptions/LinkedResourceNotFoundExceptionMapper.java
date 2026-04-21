package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        String jsonBody = String.format("{\"error\": \"LINKED_RESOURCE_NOT_FOUND\", \"message\": \"%s\"}", exception.getMessage());
        return Response.status(422) // Unprocessable Entity
                .entity(jsonBody)
                .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}
