package com.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * The core JAX-RS Application configuration.
 * Maps all REST APIs to the "/api/v1" base path as per coursework requirements.
 * Using Jersey's ResourceConfig which extends javax.ws.rs.core.Application.
 */
@ApplicationPath("/api/v1")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        // We will tell Jersey where to scan for our resources (controllers) and providers
        packages("com.smartcampus.resources", "com.smartcampus.exceptions", "com.smartcampus.filters");
    }
}
