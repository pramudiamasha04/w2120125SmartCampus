# Smart Campus API

Repository for w2120125 "Smart Campus" project (Client-Server Architectures Coursework).

## Build & Run Instructions
*(To be populated as endpoints are developed)*

## Sample cURL Commands
*(To be populated as endpoints are developed)*

---

# Coursework Report Answers

### Part 1: Service Architecture & Setup

**Question 1.1:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** 
By default, JAX-RS treats Resource classes as "per-request". This means that the JAX-RS runtime (e.g., Jersey) creates a new instance of the Resource class for every single incoming HTTP request, and objects are garbage-collected once the response is sent back. 

This architectural decision has a critical impact on how we manage in-memory data structures. If we were to declare our maps or lists as standard instance variables inside the Resource class `(e.g., private Map<String, Room> rooms = new HashMap<>();)`, every request would get its own empty map and data would be lost immediately after the request finishes. To prevent data loss and allow different requests to share the same data, we must decouple the data layer from the Resource lifecycle. We achieve this by using a centralized, in-memory Singleton `DataStore`. Additionally, because the web server handles multiple requests concurrently using multiple threads, our Singleton must use thread-safe data structures like `ConcurrentHashMap` (or properly synchronized methods) to prevent race conditions, dirty reads, or data corruption when multiple requests attempt to read or modify the state simultaneously.
