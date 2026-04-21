package com.smartcampus.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Let WebApplicationExceptions be handled by the JAX-RS default mapper
        // to avoid overriding HTTP status codes like 404 or 400.
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        // Log the actual exception server-side
        LOGGER.log(Level.SEVERE, "Unexpected error occurred: " + exception.getMessage(), exception);

        // Return a safe JSON body
        String jsonBody = "{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"An unexpected error occurred. Please contact the admin.\"}";
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(jsonBody)
                .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .build();
    }
}
