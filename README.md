# Smart Campus API

Repository for w2120125 "Smart Campus" project (Client-Server Architectures Coursework).

## Build & Run Instructions

1. Ensure you have **Java 17+** and **Maven** installed on your system.
2. In your terminal, navigate to the project root directory where `pom.xml` is located.
3. Clean and compile the project using the command:
   ```bash
   mvn clean compile
   ```
4. Start the embedded Tomcat server via Maven execution:
   ```bash
   mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
   ```
5. The API will now be running on `http://localhost:8080/api/v1/`.

## Sample cURL Commands

* **Create a Room**
  ```bash
  curl -X POST http://localhost:8080/api/v1/rooms \
    -H "Content-Type: application/json" \
    -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
  ```

* **Get All Rooms**
  ```bash
  curl -X GET http://localhost:8080/api/v1/rooms
  ```

* **Create a Sensor (Linked to Room)**
  ```bash
  curl -X POST http://localhost:8080/api/v1/sensors \
    -H "Content-Type: application/json" \
    -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","roomId":"LIB-301"}'
  ```

* **Add a Sensor Reading**
  ```bash
  curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
    -H "Content-Type: application/json" \
    -d '{"value": 23.5}'
  ```

* **Filter Sensors by Type**
  ```bash
  curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
  ```

---

# Coursework Report Answers

### Part 1: Service Architecture & Setup

**Question 1.1:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** 
By default, JAX-RS treats Resource classes as "per-request". This means that the JAX-RS runtime (e.g., Jersey) creates a new instance of the Resource class for every single incoming HTTP request, and objects are garbage-collected once the response is sent back. 

This architectural decision has a critical impact on how we manage in-memory data structures. If we were to declare our maps or lists as standard instance variables inside the Resource class `(e.g., private Map<String, Room> rooms = new HashMap<>();)`, every request would get its own empty map and data would be lost immediately after the request finishes. To prevent data loss and allow different requests to share the same data, we must decouple the data layer from the Resource lifecycle. We achieve this by using a centralized, in-memory Singleton `DataStore`. Additionally, because the web server handles multiple requests concurrently using multiple threads, our Singleton must use thread-safe data structures like `ConcurrentHashMap` (or properly synchronized methods) to prevent race conditions, dirty reads, or data corruption when multiple requests attempt to read or modify the state simultaneously.

**Question 1.2:** Why is HATEOAS beneficial?
**Answer:** HATEOAS (Hypermedia as the Engine of Application State) is beneficial because it allows clients to navigate the REST API dynamically through hypermedia links provided in the responses. Instead of hard-coding endpoint URLs in the client application, the client discovers actions and relations at runtime. This decouples the client from server architecture changes and allows the server to change URIs without breaking the client.

### Part 2: Resources & Relationships

**Question 2.1:** IDs-only vs full objects in list responses?
**Answer:** Returning IDs-only is lightweight and minimizes bandwidth usage when collections grow very large. However, it requires clients to make subsequent requests to fetch full entity details (the N+1 problem). Returning full objects provides all requisite information in a single HTTP request, reducing network latency, but consumes more bandwidth and memory per call. The choice depends on the specific use cases of the front-end rendering the data.

**Question 2.2:** Is DELETE idempotent in your implementation?
**Answer:** Yes, the DELETE endpoint is idempotent. If a client attempts to delete a room multiple times matching `/api/v1/rooms/LIB-301`, the first successful deletion will remove it from the data store and return a 204 No Content. Subsequent DELETE calls will observe that the room is missing and cleanly return a 404 Not Found without causing any state mutations. Consequently, the resulting server state is identical whether the method is called once or many times.

### Part 3: Sub-resources & Query Params

**Question 3.1:** What happens if client sends text/plain to `@Consumes(APPLICATION_JSON)`?
**Answer:** The JAX-RS runtime will intercept the incoming request and notice the `Content-Type` header is `text/plain` which does not match the `@Consumes` definition for the endpoint. It will reject the request automatically and return a `415 Unsupported Media Type` HTTP error, preventing the handler method from executing.

**Question 3.2:** `@QueryParam` vs path-based filtering?
**Answer:** `@QueryParam` is highly advantageous for filtering because query parameters semantically represent optional modifiers on an existing resource collection (e.g. `/sensors?type=Temperature` retrieves the "sensors" collection filtered down by type). Path-based filtering (like `/sensors/type/Temperature`) treats the filter conceptually as a rigid hierarchical sub-resource, which becomes inflexible when multiple independent filters (like `?type=CO2&status=ACTIVE`) need to be applied simultaneously.

### Part 4: Design Patterns

**Question 4.1:** Benefits of sub-resource locator pattern?
**Answer:** The sub-resource locator pattern drastically improves maintainability by breaking down monolithic resource classes. Instead of packing all logic into `SensorResource`, logic specific to sensor readings is encapsulated in `SensorReadingResource`. The primary resource handles identifying the parent (`sensorId`), validating its existence, and delegating the request context to the sub-resource, ensuring good Separation of Concerns and keeping the codebase modular.

### Part 5: Exception Handling & Filters

**Question 5.2:** Why 422 over 404 for missing reference?
**Answer:** Returning 404 Not Found implies that the target URL endpoint itself (e.g., `/api/v1/sensors`) does not exist. However, the client is hitting a perfectly valid endpoint. The issue is that the payload data (the `roomId` property within the JSON body) holds an invalid or unresolvable reference. `422 Unprocessable Entity` correctly signals that the server understood the content type, and the syntax is correct, but it cannot process the semantic meaning of the payload due to business logic (missing linked room).

**Question 5.4:** Cybersecurity risks of exposing stack traces?
**Answer:** Exposing raw Java stack traces leaks sensitive technical footprints about the server's backend configuration. Attackers can observe the precise libraries, framework versions, package names, and architectural decisions in use. They can then cross-reference this information with known CVE databases to orchestrate targeted exploit chains. A catch-all mapper providing a clean `500` format prevents data leakage while keeping logs internal and secure.

**Question 5.5:** Why filters vs manual logging?
**Answer:** Filters operate as interceptors across the global HTTP pipeline, allowing us to enforce observability without repeatedly rewriting `LOGGER.info(...)` blocks simultaneously across every single resource method in the application. This ensures consistent log structuring and strictly enforces the DRY (Don't Repeat Yourself) principle, cleanly separating the cross-cutting concern of logging from core business logic functionalities.
