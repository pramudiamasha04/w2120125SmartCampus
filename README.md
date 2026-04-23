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
   - Set **Context Path** to `/api/v1`.
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

**1. Project & Application Configuration**
**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** JAX-RS resources are **request-scoped** by default; a new instance is created for every request. To persist data across requests, we use a **Singleton DataStore**. We utilize thread-safe structures like `ConcurrentHashMap` to prevent race conditions and data loss when multiple threads access the state simultaneously.

**2. The "Discovery" Endpoint**
**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** Hypermedia allows for **dynamic discovery**, enabling clients to navigate the API via links rather than hardcoded URLs. This reduces coupling and allows the server to evolve its URI structure without breaking client integrations, offering more flexibility than static documentation.

### Part 2: Room Management

**1. Room Resource Implementation**
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:** Returning IDs minimizes bandwidth but causes the **N+1 problem**, requiring multiple calls for details. Full objects increase payload size but reduce overall latency and client-side processing by providing all necessary data in a single request.

**2. Room Deletion & Safety Logic**
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, it is **idempotent**. The first request deletes the room (204 No Content), and subsequent requests return 404 Not Found. The end state of the server (the room being gone) remains the same regardless of how many times the request is sent.

### Part 3: Sensor Operations & Linking

**1. Sensor Resource & Integrity**
**Question:** We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** If a client sends an unsupported format, JAX-RS intercepts the request and returns an **HTTP 415 Unsupported Media Type** error. This prevents the resource method from executing with invalid data, ensuring the API's integrity.

**2. Filtered Retrieval & Search**
**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** Query parameters are intended for **filtering collections**, while path parameters are for identifying resources. Query params are more extensible, allowing for complex combinations (e.g., `?type=CO2&status=ACTIVE`) that are difficult to manage with rigid path hierarchies.

### Part 4: Deep Nesting with Sub-Resources

**1. The Sub-Resource Locator Pattern**
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** This pattern promotes **modularity** and the **Separation of Concerns**. By delegating nested logic to dedicated sub-resource classes, we avoid "God Classes" and keep the codebase easier to maintain, test, and scale.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**2. Dependency Validation (422 Unprocessable Entity)**
**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** **404** implies the URL itself is missing. **422** accurately signals that the URL is valid and the JSON syntax is correct, but the content is semantically invalid (e.g., referencing a room ID that doesn't exist).

**4. The Global Safety Net (500)**
**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Stack traces leak **sensitive implementation details**, such as library versions and package structures, which attackers can use to identify vulnerabilities. We use a catch-all mapper to return generic 500 errors and hide these details.

**5. API Request & Response Logging Filters**
**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** Filters centralize **cross-cutting concerns**, ensuring consistent logging across all endpoints without code duplication (DRY principle). This makes the API more maintainable and prevents observability "blind spots."
