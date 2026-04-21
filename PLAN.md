# Smart Campus — 1-Day Implementation Plan

> **Goal:** Complete all 5 parts of the coursework in a single focused day, with clean git commits after each logical step.

---

## Current Project Status (What's Already Done)

| Item | Status |
|---|---|
| Maven project with embedded Tomcat + Jersey | ✅ Done |
| `RestApplication.java` (`@ApplicationPath("/api/v1")`) | ✅ Done |
| `Main.java` (embedded Tomcat bootstrap) | ✅ Done |
| POJOs: `Room`, `Sensor`, `SensorReading` | ✅ Done |
| `DataStore` singleton (ConcurrentHashMap) | ✅ Done |
| `DiscoveryResource` (GET `/api/v1`) | ✅ Done |
| README — Part 1 Q1 answer | ✅ Done |
| Room Resource (CRUD) | ❌ Not started |
| Sensor Resource (CRUD + filtering) | ❌ Not started |
| Sub-Resource (SensorReadingResource) | ❌ Not started |
| Custom exceptions + ExceptionMappers | ❌ Not started |
| Logging filter | ❌ Not started |
| README report answers (Q1.2 onwards) | ❌ Not started |

---

## Step-by-Step Plan (8 Commits)

---

### Step 1 — Room Resource: GET all & POST new room *(Part 2.1)*
**⏱ Estimated time: 30 min**

**Files to create:**
- `src/main/java/com/smartcampus/resources/RoomResource.java`

**What to implement:**
- `@Path("/rooms")` resource class
- `GET /` → return list of all rooms from `DataStore`
- `POST /` → accept JSON room, store in `DataStore`, return `201 Created` with the room in the body
- `GET /{roomId}` → return a single room by ID, or `404 Not Found` if it doesn't exist

**Postman tests:**
1. `POST /api/v1/rooms` with body `{"id":"LIB-301","name":"Library Quiet Study","capacity":50}` → expect `201`
2. `POST /api/v1/rooms` with body `{"id":"ENG-101","name":"Engineering Lab","capacity":30}` → expect `201`
3. `GET /api/v1/rooms` → expect list with 2 rooms
4. `GET /api/v1/rooms/LIB-301` → expect the room object
5. `GET /api/v1/rooms/FAKE-999` → expect `404`

**Git commit:**
```
git add -A && git commit -m "Part 2.1: Implement RoomResource with GET all, GET by ID, and POST"
```

---

### Step 2 — Room Deletion with Safety Logic *(Part 2.2)*
**⏱ Estimated time: 20 min**

**Files to create:**
- `src/main/java/com/smartcampus/exceptions/RoomNotEmptyException.java`
- `src/main/java/com/smartcampus/exceptions/RoomNotEmptyExceptionMapper.java`

**Files to modify:**
- `RoomResource.java` → add `DELETE /{roomId}` method

**What to implement:**
- `DELETE /{roomId}` → check if room exists (404 if not), check if `sensorIds` list is **not empty** → throw `RoomNotEmptyException`
- If room has no sensors → remove from DataStore, return `204 No Content`
- `RoomNotEmptyException` → custom unchecked exception with a message
- `RoomNotEmptyExceptionMapper` → `@Provider`, implements `ExceptionMapper<RoomNotEmptyException>`, returns `409 Conflict` with JSON body:
  ```json
  { "error": "ROOM_NOT_EMPTY", "message": "Cannot delete room X — it still has active sensors assigned." }
  ```

**Postman tests:**
1. `DELETE /api/v1/rooms/LIB-301` (no sensors yet) → expect `204`
2. `GET /api/v1/rooms/LIB-301` → expect `404` (deleted)
3. Re-create room, assign a sensor (in Step 3), then try delete → expect `409`

**Git commit:**
```
git add -A && git commit -m "Part 2.2: Implement room deletion with 409 conflict for non-empty rooms"
```

---

### Step 3 — Sensor Resource: POST with validation & GET all *(Part 3.1)*
**⏱ Estimated time: 30 min**

**Files to create:**
- `src/main/java/com/smartcampus/resources/SensorResource.java`
- `src/main/java/com/smartcampus/exceptions/LinkedResourceNotFoundException.java`
- `src/main/java/com/smartcampus/exceptions/LinkedResourceNotFoundExceptionMapper.java`

**What to implement:**
- `@Path("/sensors")` resource class
- `GET /` → return all sensors from DataStore
- `POST /` → accept JSON sensor body:
  - Validate that `roomId` exists in DataStore → if not, throw `LinkedResourceNotFoundException`
  - Store sensor in DataStore
  - **Link sensor to room:** add the sensor's ID to the room's `sensorIds` list
  - Return `201 Created`
- `GET /{sensorId}` → return single sensor or `404`
- `LinkedResourceNotFoundException` → custom exception
- `LinkedResourceNotFoundExceptionMapper` → returns `422 Unprocessable Entity` with JSON body:
  ```json
  { "error": "LINKED_RESOURCE_NOT_FOUND", "message": "The referenced room 'XYZ' does not exist." }
  ```

**Postman tests:**
1. Create a room first: `POST /api/v1/rooms` → `{"id":"LIB-301","name":"Library","capacity":50}`
2. `POST /api/v1/sensors` with `{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","roomId":"LIB-301"}` → `201`
3. `POST /api/v1/sensors` with `{"id":"CO2-001","type":"CO2","status":"ACTIVE","roomId":"FAKE-999"}` → `422`
4. `GET /api/v1/sensors` → expect list with TEMP-001
5. Now try `DELETE /api/v1/rooms/LIB-301` → expect `409` (has sensor)

**Git commit:**
```
git add -A && git commit -m "Part 3.1: Implement SensorResource with POST validation and 422 error handling"
```

---

### Step 4 — Sensor Filtered Retrieval *(Part 3.2)*
**⏱ Estimated time: 15 min**

**Files to modify:**
- `SensorResource.java` → enhance `GET /` with `@QueryParam("type")`

**What to implement:**
- Modify `GET /api/v1/sensors` to accept an optional `?type=` query parameter
- If `type` is provided → filter sensors by matching `sensor.getType()` (case-insensitive)
- If `type` is not provided → return all sensors

**Postman tests:**
1. Create sensors of different types (Temperature, CO2, Occupancy)
2. `GET /api/v1/sensors?type=CO2` → only CO2 sensors
3. `GET /api/v1/sensors?type=Temperature` → only Temperature sensors
4. `GET /api/v1/sensors` → all sensors

**Git commit:**
```
git add -A && git commit -m "Part 3.2: Add query parameter filtering for sensors by type"
```

---

### Step 5 — Sub-Resource: SensorReadingResource *(Part 4)*
**⏱ Estimated time: 40 min**

**Files to create:**
- `src/main/java/com/smartcampus/resources/SensorReadingResource.java`
- `src/main/java/com/smartcampus/exceptions/SensorUnavailableException.java`
- `src/main/java/com/smartcampus/exceptions/SensorUnavailableExceptionMapper.java`

**Files to modify:**
- `SensorResource.java` → add sub-resource locator method

**What to implement:**

In `SensorResource.java`:
- Add a method at `@Path("{sensorId}/readings")` that returns a **new instance** of `SensorReadingResource`, passing in the `sensorId`
- Validate sensor exists → 404 if not

In `SensorReadingResource.java` (NOT annotated with `@Path` — it's a sub-resource):
- Constructor takes `sensorId` as parameter
- `GET /` → return the list of readings for that sensor from `DataStore.sensorReadings`
- `POST /` → add a new reading:
  - Check sensor status → if `"MAINTENANCE"`, throw `SensorUnavailableException`
  - Generate UUID for the reading ID and set timestamp if not provided
  - Append to readings list in DataStore
  - **Side effect:** update the parent `Sensor.currentValue` to the new reading's value
  - Return `201 Created`

Exception classes:
- `SensorUnavailableException` → custom exception
- `SensorUnavailableExceptionMapper` → returns `403 Forbidden`:
  ```json
  { "error": "SENSOR_UNAVAILABLE", "message": "Sensor 'X' is in MAINTENANCE mode and cannot accept readings." }
  ```

**Postman tests:**
1. Create room + sensor (status=ACTIVE)
2. `POST /api/v1/sensors/TEMP-001/readings` with `{"value": 23.5}` → `201`
3. `POST /api/v1/sensors/TEMP-001/readings` with `{"value": 24.1}` → `201`
4. `GET /api/v1/sensors/TEMP-001/readings` → list of 2 readings
5. `GET /api/v1/sensors/TEMP-001` → verify `currentValue` is `24.1`
6. Create sensor with `status=MAINTENANCE` → POST reading → expect `403`

**Git commit:**
```
git add -A && git commit -m "Part 4: Implement SensorReadingResource sub-resource with 403 for maintenance sensors"
```

---

### Step 6 — Global Safety Net & Catch-All Exception Mapper *(Part 5.4)*
**⏱ Estimated time: 15 min**

**Files to create:**
- `src/main/java/com/smartcampus/exceptions/GenericExceptionMapper.java`

**What to implement:**
- `@Provider` class implementing `ExceptionMapper<Throwable>`
- Catches ANY unexpected exception that isn't already handled by specific mappers
- Returns `500 Internal Server Error` with a **safe** JSON body (no stack trace):
  ```json
  { "error": "INTERNAL_SERVER_ERROR", "message": "An unexpected error occurred. Please contact the admin." }
  ```
- Log the actual exception server-side using `java.util.logging.Logger`

**Postman tests:**
- Difficult to trigger intentionally, but this is a safety net — verify it works by temporarily throwing a `NullPointerException` in a resource method, then remove it

**Git commit:**
```
git add -A && git commit -m "Part 5.4: Add global catch-all ExceptionMapper for 500 errors"
```

---

### Step 7 — Request & Response Logging Filter *(Part 5.5)*
**⏱ Estimated time: 15 min**

**Files to create:**
- `src/main/java/com/smartcampus/filters/LoggingFilter.java`

**What to implement:**
- `@Provider` class implementing both `ContainerRequestFilter` and `ContainerResponseFilter`
- In `filter(ContainerRequestContext)` → log: `"Request: GET /api/v1/rooms"`
- In `filter(ContainerRequestContext, ContainerResponseContext)` → log: `"Response: 200"`
- Use `java.util.logging.Logger`

**Postman tests:**
- Make any API request and check the server console for log output

**Git commit:**
```
git add -A && git commit -m "Part 5.5: Add JAX-RS logging filter for request/response observability"
```

---

### Step 8 — README: Report Answers + Build Instructions + curl Commands *(All Parts)*
**⏱ Estimated time: 45 min**

**Files to modify:**
- `README.md`

**What to write:**

1. **Build & Run instructions** (step-by-step)
2. **5+ sample curl commands** covering different API parts
3. **Report answers for all remaining questions:**

| Question | Section |
|---|---|
| Q1.2: Why is HATEOAS beneficial? | Part 1 |
| Q2.1: IDs-only vs full objects in list responses? | Part 2 |
| Q2.2: Is DELETE idempotent in your implementation? | Part 2 |
| Q3.1: What happens if client sends text/plain to `@Consumes(APPLICATION_JSON)`? | Part 3 |
| Q3.2: `@QueryParam` vs path-based filtering? | Part 3 |
| Q4.1: Benefits of sub-resource locator pattern? | Part 4 |
| Q5.2: Why 422 over 404 for missing reference? | Part 5 |
| Q5.4: Cybersecurity risks of exposing stack traces? | Part 5 |
| Q5.5: Why filters vs manual logging? | Part 5 |

**Git commit:**
```
git add -A && git commit -m "Final: Complete README with report answers, build instructions, and curl examples"
```

---

## Summary Timeline

| Step | Part | Task | Est. Time |
|---|---|---|---|
| 1 | Part 2.1 | Room GET/POST + GET by ID | 30 min |
| 2 | Part 2.2 + 5.1 | Room DELETE + `409 Conflict` exception | 20 min |
| 3 | Part 3.1 + 5.2 | Sensor POST + `422` validation | 30 min |
| 4 | Part 3.2 | Sensor filtering by `?type=` | 15 min |
| 5 | Part 4 + 5.3 | Sub-resource readings + `403 Forbidden` | 40 min |
| 6 | Part 5.4 | Global `500` catch-all mapper | 15 min |
| 7 | Part 5.5 | Logging filter | 15 min |
| 8 | All | README report + curls + build instructions | 45 min |
| | | **Total estimated** | **~3.5 hrs** |

> **TIP:** After finishing all coding steps, spend time recording your **10-min Postman video demo** — walk through each Part showing the requests and responses. Make sure your camera and microphone are on!

---

## Final Project File Structure

```
src/main/java/com/smartcampus/
├── Main.java                          ← (exists)
├── config/
│   └── RestApplication.java           ← (exists)
├── db/
│   └── DataStore.java                 ← (exists)
├── models/
│   ├── Room.java                      ← (exists)
│   ├── Sensor.java                    ← (exists)
│   └── SensorReading.java             ← (exists)
├── resources/
│   ├── DiscoveryResource.java         ← (exists)
│   ├── RoomResource.java              ← Step 1
│   ├── SensorResource.java            ← Step 3
│   └── SensorReadingResource.java     ← Step 5
├── exceptions/
│   ├── RoomNotEmptyException.java             ← Step 2
│   ├── RoomNotEmptyExceptionMapper.java       ← Step 2
│   ├── LinkedResourceNotFoundException.java         ← Step 3
│   ├── LinkedResourceNotFoundExceptionMapper.java   ← Step 3
│   ├── SensorUnavailableException.java              ← Step 5
│   ├── SensorUnavailableExceptionMapper.java        ← Step 5
│   └── GenericExceptionMapper.java                  ← Step 6
└── filters/
    └── LoggingFilter.java             ← Step 7
```

---

## Important Reminders

- **No Spring Boot** — JAX-RS (Jersey) only or you get **ZERO**
- **No database** — HashMap/ArrayList only or you get **ZERO**
- **No ZIP files** — GitHub repo only or you get **ZERO**
- **Video demo is MANDATORY** — 30% of each task's mark
- Due date: **24th April 2026, 13:00**
- Test everything in **Postman** before recording the video
- Push all commits to GitHub before submission
