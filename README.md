# Smart Campus Sensor & Room Management API

A RESTful JAX-RS API for managing campus rooms and IoT sensors, built with Jersey 3 deployed on Apache Tomcat 10.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Data Models](#data-models)
5. [Endpoints Reference](#endpoints-reference)
6. [Error Handling](#error-handling)
7. [Build & Deployment Instructions](#build--deployment-instructions)
8. [Sample curl Commands](#sample-curl-commands)
9. [Conceptual Report (Q&A)](#conceptual-report-qa)

---

## API Design Overview

The Smart Campus API provides a versioned RESTful interface for facilities managers and building automation systems to interact with campus room and sensor data. All data is stored in-memory using `ConcurrentHashMap` and `ArrayList` structures — no database is used.

**Base URL:** `http://localhost:8080/api/v1`

**Key design decisions:**

- **Resource hierarchy:** Rooms are top-level resources; Sensors belong to Rooms; Sensor Readings are sub-resources of Sensors — mirroring the physical campus structure.
- **Sub-Resource Locator pattern:** `GET /sensors/{sensorId}/readings` is handled by a dedicated `SensorReadingResource` class, injected via Jersey's sub-resource locator mechanism.
- **HATEOAS Discovery:** `GET /api/v1` returns a discovery document with hypermedia links to all primary resource collections.
- **Singleton DataStore:** A thread-safe singleton (`DataStore`) holds shared state. `ConcurrentHashMap` prevents race conditions without explicit `synchronized` blocks on every read.
- **Leak-proof errors:** Every error scenario is handled by a specific `ExceptionMapper`. A global `ExceptionMapper<Throwable>` acts as the final safety net, ensuring no raw stack traces are ever returned to clients.
- **Cross-cutting observability:** A single JAX-RS filter (`ApiLoggingFilter`) logs every request method/URI and every response status code — no per-method logging needed.

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 11 |
| JAX-RS Implementation | Jersey 3.1.5 |
| JSON Serialization | Jackson 2.15.2 |
| Servlet Container | Apache Tomcat 10 |
| Build Tool | Maven 3 |
| Packaging | WAR (`ROOT.war`) |

---

## Project Structure

```
src/main/java/com/smartcampus/
├── application/
│   ├── SmartCampusApplication.java   # Jersey ResourceConfig, package scanning
│   └── DataStore.java                # Thread-safe singleton in-memory store
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java
│   └── ErrorResponse.java            # Standardised JSON error body
├── resource/
│   ├── DiscoveryResource.java        # GET /api/v1  (HATEOAS discovery)
│   ├── RoomResource.java             # /api/v1/rooms
│   ├── SensorResource.java           # /api/v1/sensors
│   └── SensorReadingResource.java    # /api/v1/sensors/{id}/readings
├── exception/
│   ├── ResourceNotFoundException.java + Mapper        # 404
│   ├── LinkedResourceNotFoundException.java + Mapper  # 422
│   ├── RoomNotEmptyException.java + Mapper            # 409
│   ├── SensorUnavailableException.java + Mapper       # 403
│   └── GlobalExceptionMapper.java                     # 500 catch-all
└── filter/
    └── ApiLoggingFilter.java          # ContainerRequest/ResponseFilter
src/main/webapp/
├── index.html                         # Landing page → redirects to /api/v1/
├── WEB-INF/web.xml                    # Servlet mapping: /api/v1/*
└── META-INF/context.xml               # Tomcat context config
```

---

## Data Models

### Room

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Unique identifier, e.g. `LIB-301` |
| `name` | String | Human-readable name |
| `capacity` | int | Maximum occupancy |
| `sensorIds` | List\<String\> | IDs of sensors deployed in this room |

### Sensor

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Unique identifier, e.g. `TEMP-001` |
| `type` | String | Category: `Temperature`, `CO2`, `Occupancy`, etc. |
| `status` | String | `ACTIVE`, `MAINTENANCE`, or `OFFLINE` |
| `currentValue` | double | Most recent measurement |
| `roomId` | String | Foreign key to the containing Room |

### SensorReading

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Auto-generated UUID |
| `timestamp` | long | Epoch milliseconds (auto-set on creation) |
| `value` | double | Recorded measurement value |

**Seed data loaded at startup:**

- Rooms: `LIB-301` (Library Quiet Study, cap 50), `LAB-102` (Computer Science Lab, cap 30)
- Sensors: `TEMP-001` (Temperature, ACTIVE, 22.5, LIB-301), `CO2-001` (CO2, ACTIVE, 412.0, LIB-301), `OCC-001` (Occupancy, MAINTENANCE, 0.0, LAB-102)

---

## Endpoints Reference

### Discovery

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| GET | `/api/v1` | 200 | HATEOAS discovery document |

### Rooms — `/api/v1/rooms`

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/rooms` | 200 | List all rooms |
| POST | `/api/v1/rooms` | 201 | Create a room |
| GET | `/api/v1/rooms/{roomId}` | 200 | Get room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | 204 | Delete room (fails with 409 if sensors assigned) |

### Sensors — `/api/v1/sensors`

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/sensors` | 200 | List all sensors |
| GET | `/api/v1/sensors?type=CO2` | 200 | List sensors filtered by type |
| POST | `/api/v1/sensors` | 201 | Register a sensor (roomId must exist) |
| GET | `/api/v1/sensors/{sensorId}` | 200 | Get sensor by ID |

### Sensor Readings — `/api/v1/sensors/{sensorId}/readings`

| Method | Path | Status | Description |
|--------|------|--------|-------------|
| GET | `/api/v1/sensors/{sensorId}/readings` | 200 | Get reading history |
| POST | `/api/v1/sensors/{sensorId}/readings` | 201 | Append a reading (fails with 403 if MAINTENANCE) |

---

## Error Handling

Every error returns a structured JSON body — no raw exceptions or stack traces are ever exposed.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found with ID: INVALID-ID"
}
```

| HTTP Status | Trigger |
|-------------|---------|
| 400 Bad Request | Missing or empty required field |
| 403 Forbidden | POST reading to a sensor with `MAINTENANCE` status |
| 404 Not Found | Resource (room or sensor) does not exist at the given path |
| 409 Conflict | Duplicate resource ID, or deleting a room that still has sensors |
| 422 Unprocessable Entity | POST sensor with a `roomId` that does not exist |
| 500 Internal Server Error | Any uncaught runtime exception (global safety net) |

---

## Build & Deployment Instructions

### Prerequisites

- JDK 11 or newer (verify: `java -version`)
- Apache Tomcat 10.x ([download](https://tomcat.apache.org/download-10.cgi))
- Maven 3.x (verify: `mvn -version`)

### Step 1 — Clone the repository

```bash
git clone https://github.com/Shara-Hasanjan/Smart_campus_API.git
cd Smart_campus_API
```

### Step 2 — Build the WAR

```bash
mvn clean package
```

On success the following file is produced:

```
target/ROOT.war
```

### Step 3 — Deploy to Tomcat

1. Stop Tomcat if it is running:
   ```bash
   # Linux/macOS
   $CATALINA_HOME/bin/shutdown.sh

   # Windows
   %CATALINA_HOME%\bin\shutdown.bat
   ```

2. Copy the WAR into Tomcat's webapps directory:
   ```bash
   # Linux/macOS
   cp target/ROOT.war $CATALINA_HOME/webapps/

   # Windows (PowerShell)
   Copy-Item target\ROOT.war $env:CATALINA_HOME\webapps\
   ```

3. Start Tomcat:
   ```bash
   # Linux/macOS
   $CATALINA_HOME/bin/startup.sh

   # Windows
   %CATALINA_HOME%\bin\startup.bat
   ```

4. Wait a few seconds for deployment, then visit:
   ```
   http://localhost:8080/
   ```
   The landing page automatically redirects to the discovery endpoint at `http://localhost:8080/api/v1/`.

> **Note:** Because the artifact is named `ROOT.war`, Tomcat deploys it at the root context (`/`). No context path prefix is needed.

### Step 4 — Verify the deployment

```bash
curl http://localhost:8080/api/v1/
```

Expected: HTTP 200 with JSON discovery document.

---

## Sample curl Commands

> All commands assume the server is running at `http://localhost:8080`.

### 1. Discovery endpoint — GET `/api/v1/`

```bash
curl -s http://localhost:8080/api/v1/
```

Returns API metadata and HATEOAS links to all primary resource collections.

---

### 2. List all rooms — GET `/api/v1/rooms`

```bash
curl -s http://localhost:8080/api/v1/rooms
```

Returns the full list of rooms including their assigned sensor IDs.

---

### 3. Create a new room — POST `/api/v1/rooms`

```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"AUD-001","name":"Main Auditorium","capacity":500}'
```

Returns HTTP 201 with the created room and a `Location` header.

---

### 4. Register a sensor in a room — POST `/api/v1/sensors`

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"HUMID-001","type":"Humidity","status":"ACTIVE","currentValue":45.5,"roomId":"AUD-001"}'
```

Returns HTTP 201. The sensor ID is automatically added to the room's `sensorIds` list. Supplying a non-existent `roomId` returns HTTP 422.

---

### 5. Filter sensors by type — GET `/api/v1/sensors?type=Temperature`

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=Temperature"
```

Returns only sensors whose `type` field matches `Temperature` (case-insensitive).

---

### 6. Add a sensor reading — POST `/api/v1/sensors/{sensorId}/readings`

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

Returns HTTP 201 with the new reading. Also updates `currentValue` on `TEMP-001`. POSTing to a sensor with status `MAINTENANCE` (e.g., `OCC-001`) returns HTTP 403.

---

### 7. Get all readings for a sensor — GET `/api/v1/sensors/{sensorId}/readings`

```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

Returns the complete historical reading log for the sensor.

---

### 8. Delete a room — DELETE `/api/v1/rooms/{roomId}`

```bash
# First create a room with no sensors
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"EMPTY-001","name":"Empty Room","capacity":10}'

# Now delete it
curl -s -X DELETE http://localhost:8080/api/v1/rooms/EMPTY-001
```

Returns HTTP 204. Attempting to delete a room that still has sensors (e.g., `LIB-301`) returns HTTP 409 Conflict.

---

### 9. Trigger the 422 error — POST sensor with missing room

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"GHOST-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"DOES-NOT-EXIST"}'
```

Returns HTTP 422 because `roomId` references a room that does not exist.

---

## Conceptual Report (Q&A)

### Part 1.1 — JAX-RS Resource Class Lifecycle

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? How does this impact in-memory data structure management?

**Answer:**

By default, JAX-RS creates a **new instance of each resource class for every incoming HTTP request** (per-request scope). This is the specification-mandated default. Each request gets its own resource object, which makes field-level state in the resource class thread-safe by isolation — no two requests share the same object instance.

However, because the resource instances are short-lived and discarded after each request, they cannot hold application state themselves. All persistent data must live outside the resource class, in a shared object that survives across requests.

In this implementation, the `DataStore` singleton pattern addresses this. A single `DataStore` instance — held in a `static` field — is shared across every resource instance. Because multiple requests can arrive concurrently and access this shared store simultaneously, `ConcurrentHashMap` is used instead of plain `HashMap`. `ConcurrentHashMap` provides thread-safe read and write operations without requiring explicit `synchronized` blocks, preventing data corruption or lost updates that would occur if two threads modified a plain `HashMap` at the same time. The combination of per-request resource instances with a shared, thread-safe singleton store is the standard JAX-RS pattern for in-memory state management.

---

### Part 1.2 — HATEOAS and the Discovery Endpoint

**Question:** Why is the provision of "Hypermedia" (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

**Answer:**

HATEOAS (Hypermedia as the Engine of Application State) means the API includes navigational links in its responses, allowing clients to discover available actions and resources dynamically rather than relying on hard-coded URLs. This is considered the highest maturity level of REST (Level 3 in the Richardson Maturity Model) because it truly decouples client and server.

The concrete benefits over static documentation are:

1. **No hard-coded URLs in clients:** A client bootstraps from the single well-known entry point (`GET /api/v1/`) and follows links from there. If the server renames or restructures a path, existing clients that follow links rather than constructing URLs continue to work without code changes.

2. **Self-describing API:** The response itself communicates what actions are possible at any given moment. A client does not need to consult out-of-date documentation to know that sensors can be filtered with `?type=`.

3. **Discoverability during development:** New team members or integration partners can explore the API interactively by following links, accelerating onboarding without needing a separate reference guide.

4. **Reduced coupling:** Static documentation describes what was true when it was written. Hypermedia reflects the current live state of the API, so clients are always working with accurate information.

---

### Part 2.1 — Returning IDs vs Full Room Objects

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**

**Returning only IDs** produces a minimal response payload. Network bandwidth consumption is very low, which matters when the collection is large or when clients are on slow connections. However, the client must then issue a separate `GET /api/v1/rooms/{id}` request for every room it wants to display or process — this is the N+1 request problem. A list of 500 rooms would require 501 HTTP round-trips, each incurring latency. The client also takes on the responsibility of aggregating the data.

**Returning full room objects** incurs higher bandwidth per response but eliminates subsequent fetches. A single request gives the client everything it needs to render a room list, apply client-side filtering, or build a UI. The additional data per object (name, capacity, sensorIds) is modest — for typical room counts the total payload remains small. The client-side code is also simpler: no coordination of parallel sub-requests or error handling for partial failures.

In this implementation, `GET /api/v1/rooms` returns full room objects. For a campus-scale deployment with hundreds of rooms, this is the correct trade-off: the bandwidth increase is negligible compared to the latency and complexity cost of per-room follow-up requests. If the collection were extremely large, pagination with full objects per page would be preferable to returning IDs.

---

### Part 2.2 — Is DELETE Idempotent?

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification describing what happens if a client sends the same DELETE request multiple times.

**Answer:**

Yes, the DELETE operation is **idempotent** in this implementation — sending the same request multiple times produces the same server state as sending it once.

Here is what happens across repeated calls:

- **First call:** The room exists, has no sensors, is removed from the `DataStore`. Response: `204 No Content`.
- **Second call (and beyond):** The room no longer exists. The resource method calls `DataStore.getRooms().get(roomId)`, gets `null`, and throws `ResourceNotFoundException`. Response: `404 Not Found`.

Idempotency in REST means the **state of the server** is identical after one call or ten calls — the resource is absent in both cases. The HTTP response code does change (204 → 404), but this does not violate idempotency; the specification allows the status code to differ on repeated calls as long as the server-side effect is the same. This aligns with RFC 9110, which defines idempotency in terms of server state, not response uniformity. A well-behaved client that receives 404 on the second DELETE simply confirms the resource is already gone and treats the operation as complete.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

**Question:** Explain the technical consequences if a client sends data in `text/plain` or `application/xml` to an endpoint annotated with `@Consumes(MediaType.APPLICATION_JSON)`.

**Answer:**

When `@Consumes(MediaType.APPLICATION_JSON)` is declared on a method, JAX-RS uses the incoming request's `Content-Type` header during **request matching** before the method body is ever invoked.

If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, the Jersey runtime cannot find a resource method that declares it consumes that media type. The request matching algorithm fails at the media type negotiation step and Jersey automatically returns **HTTP 415 Unsupported Media Type** — no application code runs at all.

If a client sends `Content-Type: application/json` but the body is actually malformed JSON or an XML document with a wrong header, Jersey finds the matching method but then attempts to deserialize the body using Jackson. Jackson fails to parse the body and throws a `JsonParseException` or similar. This bubbles up through Jersey and would normally produce a 400-level error; in this implementation it is caught by the global `ExceptionMapper<Throwable>` and returned as HTTP 500 with a generic message (since `JsonParseException` is not a mapped exception type). Extending the exception mapping to handle `JsonParseException` specifically with a 400 response would be a production improvement.

In short: wrong `Content-Type` header → 415 before method runs. Correct header but wrong body → Jackson deserialization failure → caught by global mapper.

---

### Part 3.2 — @QueryParam vs Path-Based Filtering

**Question:** Why is the query parameter approach (`?type=CO2`) generally considered superior to path-based filtering (`/sensors/type/CO2`) for filtering collections?

**Answer:**

The `@QueryParam` approach is correct for several reasons:

1. **Semantics of the resource:** The path `/api/v1/sensors` identifies the *sensors collection* as a resource. Adding `/type/CO2` to the path implies `CO2 sensors` is a distinct, addressable resource with its own identity — which is incorrect. Filtering is a view into a collection, not a new resource. Query parameters communicate "apply this filter to the existing resource" rather than "navigate to a different resource."

2. **Optional and composable:** Query parameters are inherently optional. `GET /sensors` and `GET /sensors?type=CO2` share the same resource path; only one endpoint method is needed. Path segments cannot be made optional in standard JAX-RS routing. Multiple filters (e.g., `?type=CO2&status=ACTIVE`) compose naturally as query parameters but would produce unwieldy and hard-to-route path combinations.

3. **Caching and bookmarking:** The base resource `/sensors` has a stable canonical URL that can be cached aggressively. A filtered URL like `/sensors?type=CO2` is understood by HTTP caches to be a variant of the collection, not a separate resource.

4. **REST conventions:** RFC 3986 defines query components as non-hierarchical and intended for identifying resources within the scope of the path. Filtering parameters fit precisely that definition. Path segments should reflect the hierarchy of resources, not search parameters.

---

### Part 4.1 — Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs?

**Answer:**

The Sub-Resource Locator pattern means a resource method returns an **object instance** (instead of a `Response`) that Jersey then uses to handle the remainder of the URI. In this project, `SensorResource.getReadingResource()` is annotated with `@Path("{sensorId}/readings")` but no HTTP verb annotation. Jersey calls this method to obtain a `SensorReadingResource` instance, then dispatches the actual GET or POST to that instance.

The architectural benefits are:

1. **Single Responsibility:** `SensorResource` is responsible for `/sensors` and `/sensors/{id}` concerns only. `SensorReadingResource` owns the readings lifecycle. Neither class needs to know the implementation details of the other, making each independently testable and maintainable.

2. **Encapsulated context:** The sensor ID is passed into `SensorReadingResource`'s constructor at location time. Every method inside `SensorReadingResource` implicitly knows which sensor it is operating on without having to accept or validate the sensor ID again. The sensor existence check is done once, in the locator, before control passes to the sub-resource.

3. **Scalability of the codebase:** In a real campus API with dozens of nested resource types (rooms → sensors → readings → calibrations → alerts), putting all endpoint methods into one class produces a monolithic controller that becomes unnavigable. Sub-resource locators allow each level of the hierarchy to have its own focused class, matching the logical structure of the API to the physical structure of the code.

4. **Reusability:** A `SensorReadingResource` class constructed with a sensor ID can in principle be composed into multiple parent resources without duplicating reading logic.

---

### Part 5.2 — HTTP 422 vs 404 for Missing Reference

**Question:** Why is HTTP 422 often considered more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**

HTTP 404 Not Found means "the resource identified by the **request URI** does not exist." When a client POSTs to `/api/v1/sensors`, that URI is valid and the sensors collection does exist — a 404 would be misleading.

HTTP 422 Unprocessable Entity means "the server understands the request format and the syntax is correct, but the **semantic content** of the payload is invalid." The payload `{"roomId": "NONEXISTENT"}` is syntactically valid JSON; the problem is that the *value* of `roomId` references an entity that does not exist in the system — a semantic, not syntactic, error.

Using 422 conveys precisely what went wrong: the request was well-formed but logically inconsistent with the current state of the server. The client can use this to understand that it needs to create the room first before retrying — a 404 response would leave the client uncertain about whether the sensor endpoint itself was at fault, whereas a 422 with a clear message (`roomId 'X' does not exist`) points directly to the corrective action required.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers.

**Answer:**

A Java stack trace is a detailed map of the application's internals. Exposing it to external consumers creates several attack surfaces:

1. **Technology fingerprinting:** A stack trace reveals the exact framework names and version numbers (Jersey 3.1.5, Jackson 2.15.2, Tomcat 10). An attacker can cross-reference these against public CVE databases to find known vulnerabilities and craft targeted exploits.

2. **Internal package and class structure disclosure:** The fully-qualified class names in a trace (e.g., `com.smartcampus.application.DataStore`) reveal the application's package hierarchy and architecture. This information accelerates reverse-engineering and helps an attacker understand which classes hold critical logic.

3. **File system path disclosure:** Stack traces often include absolute file paths to source files or JAR locations on the server. This reveals directory structure, deployment paths, and potentially usernames from file paths (e.g., `/home/deploy/apps/smart-campus/`).

4. **Logic and data flow inference:** The sequence of method calls reveals business logic, control flow, and which data structures are used. An attacker can infer where validation occurs and look for ways to bypass it.

5. **Targeted injection attacks:** If a stack trace is triggered by a deliberate malformed input, the attacker can observe exactly which parser or method failed, helping craft increasingly precise injection payloads (SQL injection, JNDI injection, deserialization attacks).

The global `ExceptionMapper<Throwable>` in this project mitigates all of these by logging the full trace server-side (where it is useful for debugging) while returning only a generic "An unexpected error occurred" message to the client.

---

### Part 5.5 — JAX-RS Filters vs Per-Method Logging

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging rather than manually inserting Logger statements inside every resource method?

**Answer:**

Cross-cutting concerns are behaviours that apply uniformly across many components. Implementing them via filters rather than inline code has several concrete advantages:

1. **No code duplication:** With a filter, the logging logic exists in one place. Without a filter, every resource method — currently ten endpoints — would need its own `Logger.info()` call at entry and exit. Duplication means ten places to update if the log format changes, and ten places where developers can forget to add logging when writing new endpoints.

2. **Consistency:** A filter guarantees that every single request is logged with the same format and at the same point in the request lifecycle, including requests that are rejected before reaching a resource method (e.g., a 415 Unsupported Media Type rejection). Per-method logging cannot catch those cases.

3. **Separation of concerns:** Resource methods should express business logic — what a request does to data. Logging is infrastructure. Mixing them makes methods harder to read and test. A filter keeps each layer responsible for one thing.

4. **Maintainability and extensibility:** Adding request tracing IDs, timing measurements, or security audit logs requires changing one filter class, not every resource method. Adding a new resource class automatically inherits all filter behaviour without any developer action.

5. **Declarative application:** Filters can be scoped with `@NameBinding` to apply only to specific resources if needed, without touching the resource code itself. This gives fine-grained control without coupling infrastructure to business logic.
