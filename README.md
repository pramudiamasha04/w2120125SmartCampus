## Coursework Report Answers

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
