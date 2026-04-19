package com.smartcampus.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Resource — the root entry point for the Smart Campus API.
 * Provides API metadata and hypermedia links to all available resources,
 * enabling clients to discover endpoints without hardcoding URLs (HATEOAS).
 */
@Path("/")
public class DiscoveryResource {

    /**
     * GET /api/v1
     * Returns API metadata (name, version, description) and navigation links
     * to the main resource collections.
     *
     * @return JSON object with API info and hypermedia links
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiRoot() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", "Smart Campus API");
        root.put("version", "1.0");
        root.put("description", "RESTful API for the Smart Campus IoT initiative");

        // Hypermedia links for client navigation
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        root.put("links", links);

        return Response.ok(root).build();
    }
}
