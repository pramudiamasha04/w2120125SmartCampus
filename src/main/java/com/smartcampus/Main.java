package com.smartcampus;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;
import com.smartcampus.config.RestApplication;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create an embedded Tomcat server
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector(); // Trigger the creation of the default connector

        // Set up the context path to /api/v1
        Context context = tomcat.addContext("/api/v1", new File("src/main/webapp").getAbsolutePath());

        // Configure the Jersey ServletContainer
        Tomcat.addServlet(context, "jersey-container-servlet", 
            new ServletContainer(new RestApplication()));

        // Map all requests within the context to the Jersey servlet
        context.addServletMappingDecoded("/*", "jersey-container-servlet");

        // Start the server and keep it running
        System.out.println("Starting embedded Tomcat server on http://localhost:8080/api/v1 ...");
        tomcat.start();
        tomcat.getServer().await();
    }
}
