# Smart Campus API

The **Smart Campus API** is a robust, RESTful web service designed to manage IoT infrastructure within a university environment. Built as part of the Client-Server Architectures coursework, it provides endpoints for managing Rooms, Sensors, and Sensor Readings with a focus on thread-safety, hypermedia navigation (HATEOAS), and strong error handling.

---

## 🏗 API Design Overview

The API follows a modular, resource-oriented architecture:

- **Framework**: Developed using **JAX-RS (Jersey)** on **Java 17**.
- **Server**: Optimized for **Embedded Tomcat 9** and **Jetty 9**.
- **Data Persistence**: Implements a thread-safe, in-memory **Singleton DataStore** using `ConcurrentHashMap`. This ensures high performance and data integrity without the overhead of an external database, adhering strictly to coursework requirements.
- **HATEOAS**: Features a root **Discovery Endpoint** (`/api/v1`) that provides dynamic links to all major resource collections, allowing clients to navigate the API without hardcoding URIs.
- **Architecture**: Uses the **Sub-resource Locator** pattern for managing sensor readings, ensuring a clean separation of concerns between sensors and their historical data.
- **Safety**: Includes global exception mapping and JAX-RS filters for consistent logging and security (no stack trace exposure).

---

## 🚀 Build & Launch Instructions

You can build and run this project using one of the following three options. Ensure you have **Java 17** and **Maven** installed.

### Option 1: NetBeans IDE (Tomcat 9 & Java EE 8)
Ideal for developers using the NetBeans environment with a local application server.
1. Open **NetBeans IDE**.
2. Go to **File > Open Project** and select the project root directory.
3. Right-click the project in the sidebar and select **Properties**.
4. In the **Run** category:
   - Set **Server** to **Apache Tomcat 9.0**.
   - Set **Java EE Version** to **Java EE 8 Web**.
   - Set **Context Path** to `/`.
5. Right-click the project and select **Run**. NetBeans will automatically build the WAR, deploy it to your local Tomcat, and launch the service.
6. The API will be available at: `http://localhost:8080/api/v1/`

### Option 2: Maven Jetty Plugin
Runs the application using the integrated Jetty development server via the command line.
1. Open your terminal in the project root.
2. Run the following command:
   ```bash
   mvn jetty:run
   ```
3. The API will be available at: `http://localhost:8080/api/v1/`

### Option 3: Production WAR Build
Packages the application into a standard Web Archive (WAR) for deployment to external servers (like standalone Tomcat or Glassfish).
1. Run the package command:
   ```bash
   mvn clean package
   ```
2. The compiled artifact will be generated at: `target/ROOT.war`.
3. You can deploy this WAR file to any standard Java servlet container's `webapps` directory.

---

## 📡 Sample cURL Commands

Demonstrating core API functionality:

1. **Service Discovery (HATEOAS)**
   ```bash
   curl -X GET http://localhost:8080/api/v1/
   ```

2. **Create a New Room**
   ```bash
   curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
   ```

3. **Register a Sensor (Linked to Room)**
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","roomId":"LIB-301"}'
   ```

4. **Add a Sensor Reading**
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 23.5}'
   ```

5. **Filter Sensors by Type**
   ```bash
   curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
   ```

---

## 📝 Coursework Report Answers

### Part 1: Service Architecture & Setup
**Question 1.1: JAX-RS Resource Lifecycle & Data Management**
By default, JAX-RS resources are request-scoped; a new instance is created for every HTTP request. To prevent data loss between requests, we decouple the data layer into a Singleton `DataStore`. We use `ConcurrentHashMap` to ensure thread-safety, preventing race conditions when multiple concurrent threads access or modify the campus state.

**Question 1.2: Benefits of HATEOAS**
HATEOAS (Hypermedia as the Engine of Application State) decouples the client from the server's URI structure. By providing navigation links in the response (see `DiscoveryResource`), the server can evolve its endpoint structure without breaking clients, as they discover actions dynamically at runtime.

### Part 2: Resources & Relationships
**Question 2.1: IDs-only vs Full Objects in Responses**
Returning IDs-only is bandwidth-efficient for large lists but leads to the "N+1 problem" where clients must make multiple calls for details. Full objects reduce latency by providing everything in one call but increase payload size. Our API balances this by providing full objects for better developer experience in small-scale IoT management.

**Question 2.2: Idempotency of DELETE**
Yes, our DELETE implementation is idempotent. The first call removes the resource (204 No Content), and subsequent calls return 404 Not Found. While the status codes differ, the side-effect on the server (the resource being gone) remains identical regardless of the number of calls.

### Part 3: Sub-resources & Query Params
**Question 3.1: Content-Type Mismatch Handling**
If a client sends `text/plain` to an endpoint marked `@Consumes(APPLICATION_JSON)`, the JAX-RS runtime automatically rejects the request with a `415 Unsupported Media Type` error before the method logic is even executed.

**Question 3.2: QueryParam vs Path-based Filtering**
Query parameters (`?type=...`) are ideal for optional modifiers like filtering or sorting on a collection. Path parameters are better suited for hierarchical identification (e.g., a specific resource ID). Query parameters allow for flexible combinations (e.g., `?type=CO2&status=ACTIVE`) which is difficult with path-based routing.

### Part 4: Design Patterns
**Question 4.1: Sub-resource Locator Benefits**
The Sub-resource Locator pattern (used for `/sensors/{id}/readings`) improves code modularity and maintainability. It allows us to delegate reading-specific logic to a dedicated `SensorReadingResource` class, keeping the main `SensorResource` focused on sensor management and ensuring a clean Separation of Concerns.

### Part 5: Exception Handling & Filters
**Question 5.1: Why 409 Conflict for Room Deletion?**
A `409 Conflict` is returned if a user tries to delete a room that still has sensors assigned. This indicates a state conflict; the operation cannot proceed because it would violate data integrity (creating orphaned sensors).

**Question 5.2: Why 422 Unprocessable Entity for Missing References?**
`422` is used when a POST request contains a `roomId` that doesn't exist. The request is syntactically correct (valid JSON), but semantically invalid because the referenced resource is missing. This is more precise than `400 Bad Request` or `404 Not Found`.

**Question 5.3: Why 403 Forbidden for Maintenance Mode?**
When a sensor is in `MAINTENANCE` mode, adding readings is logically prohibited. `403 Forbidden` correctly signals that the server understands the request but refuses to fulfill it due to the specific business state of the resource.

**Question 5.4: Risks of Exposing Stack Traces**
Exposing stack traces leaks internal implementation details (package names, library versions, file paths) to potential attackers. This information can be used to identify vulnerabilities. We use a `GenericExceptionMapper` to return a safe `500 Internal Server Error` message instead.

**Question 5.5: Filters vs Manual Logging**
JAX-RS filters (`LoggingFilter`) provide a centralized, cross-cutting solution for observability. This avoids code duplication across every resource method (DRY principle) and ensures that every request/response pair is logged consistently.
