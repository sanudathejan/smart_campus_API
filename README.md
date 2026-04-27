# Campus Room & Sensor Management API

A RESTful API built with **JAX-RS (Jersey)** for managing university campus rooms, sensors, and sensor reading history.

---

## 1. API Design Overview

The API is organised around three core resources that mirror the physical structure of a campus:

- **Rooms** — physical spaces that contain sensors
- **Sensors** — devices installed inside rooms that record measurements
- **Readings** — historical data points captured by a specific sensor

**Base URL:** `http://localhost:8080/api/v1`

**Resource hierarchy:**

```
/api/v1                              ← Discovery (HATEOAS entry point)
/api/v1/rooms                        ← Room collection
/api/v1/rooms/{roomId}               ← Individual room
/api/v1/sensors                      ← Sensor collection (filterable by ?type=)
/api/v1/sensors/{sensorId}           ← Individual sensor
/api/v1/sensors/{sensorId}/readings  ← Sensor reading history (sub-resource)
```

**Architecture decisions:**
- **In-memory storage** using `ConcurrentHashMap` inside a static Singleton (`InMemoryRegistry`) — thread-safe without requiring a database
- **Sub-resource locator pattern** for reading history — keeps `SensorResource` focused on sensor metadata while delegating history management to a dedicated `ReadingResource` class
- **Custom exception mappers** for every error scenario — no raw stack traces are ever exposed to API consumers
- **JAX-RS filter** handles request/response logging as a cross-cutting concern — no logging code inside resource methods

---

## 2. Build & Launch Instructions

### Prerequisites

- Java JDK 11 or higher
- Apache Maven 3.6+
- Apache Tomcat 9.x

### Step 1 — Clone the repository

```bash
git clone <your-repo-url>
cd CampusAPI
```

### Step 2 — Build the project

```bash
mvn clean package
```

This produces `target/ROOT.war`.

### Step 3 — Deploy to Tomcat

Copy the WAR into Tomcat's webapps directory:

```bash
cp target/ROOT.war /path/to/tomcat/webapps/ROOT.war
```

### Step 4 — Start Tomcat

```bash
# Linux / macOS
/path/to/tomcat/bin/startup.sh

# Windows
/path/to/tomcat/bin/startup.bat
```

### Step 5 — Verify

Visit the discovery endpoint:

```
http://localhost:8080/api/v1
```

You should receive a JSON object with API metadata and resource links.

---

## 3. Sample curl Commands

### 1. Create a Room (POST)
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LAB-101", "name": "Computer Lab A", "capacity": 40}'
```

### 2. Register a Sensor linked to that Room (POST)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-001", "type": "CO2", "status": "ACTIVE", "roomId": "LAB-101"}'
```

### 3. Filter Sensors by type (GET with query parameter)
```bash
curl "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 4. Post a Reading to the sub-resource (POST)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"measurement": 812.5}'
```

### 5. Attempt to delete a Room that still has sensors — triggers 409 Conflict (DELETE)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LAB-101
```

### 6. Register a Sensor with a non-existent roomId — triggers 422 (POST)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"type": "Temperature", "roomId": "DOES-NOT-EXIST"}'
```

---

## 4. Conceptual Report — Question Answers

---

### Part 1.1 — JAX-RS Resource Lifecycle and In-Memory Data Management

By default, JAX-RS operates on a **per-request lifecycle**: a brand-new instance of each resource class is created for every incoming HTTP request, and that instance is discarded once the response is sent. If data were stored as instance variables inside a resource class, it would be lost after every single call, making it impossible to maintain state between requests.

To work around this, this project uses a **static Singleton pattern** (`InMemoryRegistry`) where all three data collections are declared as `static final` fields. Because static fields belong to the class, not to any instance, they persist for the entire lifetime of the JVM (i.e. until the server restarts), regardless of how many resource instances are created and destroyed.

For thread safety, `ConcurrentHashMap` is used instead of a plain `HashMap`. Tomcat handles multiple simultaneous HTTP requests on separate threads. If two requests attempted to write to a standard `HashMap` at the same moment, the result would be a data race — potentially causing corrupted data or a `ConcurrentModificationException`. `ConcurrentHashMap` uses fine-grained segment locking internally, allowing concurrent reads and safe concurrent writes without blocking the entire map on every operation.

---

### Part 1.2 — HATEOAS and Hypermedia in RESTful APIs

HATEOAS (Hypermedia As The Engine Of Application State) is the principle that API responses should embed links to related operations and resources, rather than requiring clients to construct URLs from external documentation.

In this API, the discovery endpoint (`GET /api/v1`) returns a `resources` map containing the URLs for rooms and sensors. A client that starts at the discovery endpoint can navigate the entire API by following the links provided at runtime, rather than relying on a static document that may have drifted out of sync with the actual implementation.

The benefit for client developers is **decoupling**: if the server restructures its URL patterns in a future version, clients that navigate via embedded links continue to function correctly. Clients using hardcoded URLs from documentation break immediately. HATEOAS also serves as self-documentation — a developer can explore the API by inspecting responses, even without reading a separate guide.

---

### Part 2.1 — Returning IDs vs Full Objects from a Collection Endpoint

Returning **only IDs** from `GET /rooms` minimises response payload size and network bandwidth consumption, which benefits performance when the client only needs a list of identifiers. However, it creates the **N+1 problem**: if the client needs to display metadata for each room, it must make N additional HTTP requests — one per room — which can be slow and wasteful.

Returning **full room objects** in a single response increases payload size but eliminates all follow-up requests, making the client simpler and more responsive. This is the approach taken in this implementation, because campus management clients are likely to display room details immediately after fetching the list.

---

### Part 2.2 — Is DELETE Idempotent in This Implementation?

Idempotency means that making the same request multiple times produces the same server state as making it once. In terms of **server state**, this DELETE implementation is idempotent: after the first successful deletion, the room is gone. Subsequent calls do not change the server state further — the room is still absent.

However, the **HTTP status code** differs between calls: the first call returns `200 OK`, and subsequent calls return `404 Not Found` (because the room no longer exists to be found). This is a deliberate design choice to give clients clear, honest feedback that the resource they are referencing is gone. The REST specification permits 404 on repeated DELETE calls, and most REST style guides consider this acceptable because the server state remains stable after the first call.

---

### Part 3.1 — @Consumes and Content-Type Mismatches

The `@Consumes(MediaType.APPLICATION_JSON)` annotation instructs Jersey to only invoke the resource method when the incoming request carries a `Content-Type: application/json` header. Jersey inspects this header **before** calling the method.

If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, Jersey detects the mismatch at the framework level and immediately returns **HTTP 415 Unsupported Media Type** — the resource method is never executed. This protects the application from attempting to deserialise data in an unexpected format, which could produce cryptic errors or corrupt the data store.

---

### Part 3.2 — @QueryParam vs Path-Based Filtering

Using `@QueryParam("type")` (e.g. `GET /sensors?type=CO2`) is the correct REST approach for filtering a collection for several reasons:

**URL path segments identify resources** — they represent nouns in the API's resource model. `/sensors/type/CO2` implies that `type/CO2` is itself a resource with an identity, which is semantically misleading.

**Query parameters are optional modifiers** — they describe how to retrieve a collection without altering its identity. This makes filters composable: `?type=CO2&status=ACTIVE` works naturally without creating new route definitions.

**Routing complexity** — if type were encoded in the path, the server would need a dedicated route for every possible filter combination, causing an explosion in the routing table. With query parameters, a single route handles all combinations.

---

### Part 4.1 — Sub-Resource Locator Pattern Benefits

The sub-resource locator pattern delegates a nested URL segment to a separate class rather than handling it inline. In this API, `SensorResource` contains no code for reading history — it simply instantiates and returns a `ReadingResource` when the `/readings` path is accessed.

This applies the **Single Responsibility Principle**: `SensorResource` is responsible only for sensor metadata (create, retrieve, update, delete sensors). `ReadingResource` is responsible only for reading history. Each class is smaller, more focused, and independently testable.

In a large API with dozens of nested sub-resources, placing every endpoint in one controller class creates a "God class" that becomes extremely difficult to navigate, maintain, and extend safely. Sub-resource locators enforce a logical separation that mirrors the actual resource hierarchy, making the codebase far easier to understand at a glance.

---

### Part 5.1 — Why HTTP 422 is More Accurate Than 404 for Payload Reference Errors

`404 Not Found` signals that the **requested URL path** does not exist on the server. When a client sends `POST /api/v1/sensors` with a `roomId` pointing to a non-existent room, the target URL (`/api/v1/sensors`) is completely valid and reachable.

The problem is not the URL — it is that the **JSON payload contains a reference to a resource that cannot be resolved**. The request is syntactically correct (valid JSON, correct Content-Type) but semantically invalid (a broken foreign-key reference). `422 Unprocessable Entity` was introduced precisely for this scenario: the server understands the request format but cannot process it due to a logical error in the content. Using 422 gives clients a much clearer signal about where to look to fix the problem.

---

### Part 5.2 — Security Risks of Exposing Java Stack Traces

A raw Java stack trace exposes several categories of sensitive information:

1. **Internal class names and package structure** — reveals the application's architecture, naming conventions, and which framework components are in use (e.g. `org.glassfish.jersey`, `uk.ac.campus.api`).
2. **Library names and versions** — an attacker can look up CVEs (Common Vulnerabilities and Exposures) for the exact versions revealed (e.g. Jersey 2.41, Tomcat 9.0.x) and target known exploits.
3. **Exact failure location** — the file name and line number shown in the trace reveals precisely which code path triggered the error, enabling attackers to craft payloads that reliably reproduce the failure for exploitation.
4. **Business logic leakage** — method names and class names hint at the internal design, making it easier to reverse-engineer the application's behaviour without access to the source code.

This API prevents all of the above by catching all `Throwable` instances in `GlobalExceptionMapper`, logging the full trace server-side only, and returning a generic, information-free message to the client.

### Part 5.3 — Why JAX-RS Filters Are Superior to Per-Method Logging

Logging is a **cross-cutting concern** — it applies identically to every endpoint in the API regardless of business logic. Inserting `Logger.info()` calls into every resource method violates the **DRY (Don't Repeat Yourself)** principle and the **Single Responsibility Principle**.

With 15+ endpoints, a developer could easily forget to add logging to a newly created route. A `ContainerRequestFilter` and `ContainerResponseFilter` run automatically for every request through a single registered class — coverage is guaranteed for all current and future endpoints with zero additional code in resource classes. Changing the log format, log level, or adding a correlation ID only requires editing one file, rather than touching every resource method across the codebase.
