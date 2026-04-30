# Smart Campus API - Sensor & Room Management System

## Overview

The **Smart Campus API** is a comprehensive RESTful web service for managing IoT sensors and rooms across a university campus. Built with Java JAX-RS (Jakarta RESTful Web Services), this API provides facilities managers and automated building systems with a robust, scalable interface to interact with thousands of sensors and rooms.

### Project Context
This is a coursework submission for **5COSC022W - Client-Server Architectures** at the University of Westminster, demonstrating industry-standard REST API design principles, JAX-RS framework expertise, and professional error-handling strategies.

### Quick Links
- **GitHub Repository**: https://github.com/sanudathejan/smart_campus_API  
- **Base URL**: `http://localhost:8080/smart-campus-api/api/v1`
- **Framework**: JAX-RS 3.1.5 (Jersey) + Jackson + Apache Tomcat
- **API Version**: 1.0.0
- **Developer**: Sanuda

---

## Features

✅ **RESTful Architecture** - Proper HTTP methods, status codes, and resource URIs  
✅ **HATEOAS Support** - Discovery endpoint with dynamic resource navigation  
✅ **Sub-Resource Nesting** - Hierarchical sensor readings endpoint  
✅ **Business Logic Constraints** - Prevents invalid operations  
✅ **Comprehensive Error Handling** - Semantic HTTP status codes with JSON responses  
✅ **Exception Mapping** - Never exposes stack traces to clients  
✅ **Thread-Safe Storage** - ConcurrentHashMap prevents race conditions  

---

## Technology Stack

| Component | Version |
|-----------|---------|
| Language | Java 11 |
| Framework | JAX-RS 3.1.5 (Jersey) |
| JSON Processing | Jackson 2.15.2 |
| Servlet Container | Apache Tomcat 10 |
| Build Tool | Maven 3 |
| Packaging | WAR (ROOT.war) |

---

## Installation & Setup

### Prerequisites
- Java 11+ installed and in PATH
- Maven 3.8.1+ installed
- Apache Tomcat 9+ (or use Maven plugin)
- Git for cloning repository

### Step 1: Clone Repository
```bash
git clone https://github.com/sanudathejan/smart_campus_API.git
cd smart_campus_API
```

### Step 2: Build the Project
```bash
mvn clean package
```

**Expected Output:**
```
[INFO] Building war: .../target/ROOT.war
[INFO] BUILD SUCCESS
```

### Step 3: Run Locally (Option A - Maven Plugin)
```bash
mvn tomcat7:run
```
Server starts on `http://localhost:8080/smart-campus-api/api/v1/`

### Step 3: Deploy to Tomcat (Option B - Manual)
1. Copy `target/ROOT.war` to Tomcat `webapps/` directory
2. Start Tomcat: `catalina.sh run` (Linux/Mac) or `catalina.bat run` (Windows)
3. Access: `http://localhost:8080/smart-campus-api/api/v1/`

### Step 4: Verify Installation
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/" \
  -H "Accept: application/json"
```

You should receive API discovery metadata in JSON format.

---

## API Endpoints

### Base URL
```
http://localhost:8080/smart-campus-api/api/v1
```

### Discovery Endpoint
```
GET /                    - API metadata with HATEOAS links
```

### Room Management
```
GET    /rooms            - List all rooms
POST   /rooms            - Create new room
GET    /rooms/{roomId}   - Get specific room
DELETE /rooms/{roomId}   - Delete room (only if no sensors)
```

### Sensor Operations
```
GET    /sensors                   - List all sensors
GET    /sensors?type={type}       - Filter sensors by type
POST   /sensors                   - Create new sensor
GET    /sensors/{sensorId}        - Get specific sensor
```

### Sensor Readings (Sub-Resource)
```
GET    /sensors/{sensorId}/readings      - Get all readings
POST   /sensors/{sensorId}/readings      - Add new reading
```

---

## Sample API Requests (6 curl Commands)

### Sample 1: Discovery Endpoint
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/" \
  -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
{
  "api": "Smart Campus Sensor and Room Management API",
  "version": "1.0.0",
  "description": "RESTful API for managing campus rooms and IoT sensors.",
  "contact": {
    "name": "Campus Facilities Team",
    "email": "admin@smartcampus.ac.uk"
  },
  "resources": {
    "rooms": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/rooms",
      "methods": "GET, POST"
    }
  }
}
```

---

### Sample 2: List All Rooms
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50,
    "sensorIds": ["TEMP-001", "CO2-001"]
  },
  {
    "id": "LAB-102",
    "name": "Computer Science Lab",
    "capacity": 30,
    "sensorIds": ["OCC-001"]
  }
]
```

---

### Sample 3: Create New Room
```bash
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ENG-201",
    "name": "Engineering Workshop",
    "capacity": 40
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "ENG-201",
  "name": "Engineering Workshop",
  "capacity": 40,
  "sensorIds": []
}
```

---

### Sample 4: Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  }
]
```

---

### Sample 5: Add Sensor Reading
```bash
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings" \
  -H "Content-Type: application/json" \
  -d '{"value": 23.5}'
```

**Expected Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1714521600000,
  "value": 23.5
}
```

---

### Sample 6: Delete Room with Conflict Error
```bash
curl -X DELETE "http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301"
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It currently has 2 sensor(s) assigned. Decommission all sensors before removing the room."
}
```

---

## HTTP Status Codes

| Code | Meaning | Scenario |
|------|---------|----------|
| 200 | OK | Successful GET request |
| 201 | Created | Successful POST request |
| 204 | No Content | Successful DELETE request |
| 400 | Bad Request | Missing required fields |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Room has sensors, cannot delete |
| 422 | Unprocessable Entity | Invalid foreign key reference |
| 403 | Forbidden | Sensor in MAINTENANCE status |
| 500 | Internal Error | Unexpected server error |

---

## Error Handling

All errors return structured JSON with consistent format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found with ID: INVALID-ID"
}
```

### Error Scenarios

- **400**: POST room without `id` or `name` field
- **404**: GET /rooms/NO-ROOM (non-existent room)
- **409**: DELETE /rooms/LIB-301 (room has sensors)
- **422**: POST sensor with non-existent roomId
- **403**: POST reading to maintenance sensor (OCC-001)
- **500**: Unexpected runtime exception

No raw Java stack traces are ever exposed to clients.

---

## Seed Data

Pre-populated data at startup:

**Rooms:**
- LIB-301: Library Quiet Study (capacity 50)
- LAB-102: Computer Science Lab (capacity 30)

**Sensors:**
- TEMP-001: Temperature (ACTIVE, 22.5°C, in LIB-301)
- CO2-001: CO2 monitor (ACTIVE, 412.0 ppm, in LIB-301)
- OCC-001: Occupancy tracker (MAINTENANCE, 0, in LAB-102)

---

## Project Structure

```
src/main/java/com/smartcampus/
├── application/
│   ├── SmartCampusApplication.java    - JAX-RS ResourceConfig
│   └── DataStore.java                 - Thread-safe singleton storage
├── resource/
│   ├── DiscoveryResource.java         - GET / (HATEOAS)
│   ├── RoomResource.java              - /rooms (Part 2)
│   ├── SensorResource.java            - /sensors (Part 3)
│   └── SensorReadingResource.java     - /sensors/{id}/readings (Part 4)
├── exception/
│   ├── ResourceNotFoundException.java + Mapper
│   ├── LinkedResourceNotFoundException.java + Mapper
│   ├── RoomNotEmptyException.java + Mapper
│   ├── SensorUnavailableException.java + Mapper
│   └── GlobalExceptionMapper.java     - 500 catch-all
├── model/
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java
│   └── ErrorResponse.java
└── filter/
    └── ApiLoggingFilter.java
```

---

# Conceptual Questions & Answers

---

## Part 1: Service Architecture & Setup

### Q1.1: JAX-RS Resource Lifecycle and Data Synchronization

**Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:**

By default, JAX-RS (Jersey) instantiates a **new Resource instance per HTTP request**. Each incoming request to `/api/v1/rooms` creates a fresh `RoomResource` object, which is discarded after the response is sent.

This per-request approach provides thread safety at the resource class level by isolation — each request's resource instance is not shared with other threads. However, it creates a challenge: persistent application state cannot live in resource fields.

**My Solution: Singleton DataStore Pattern**

I address this using the **Singleton pattern**. The `DataStore` class is instantiated once and shared globally:

```java
public class DataStore {
    private static final DataStore INSTANCE = new DataStore();
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    public static DataStore getInstance() {
        return INSTANCE;  // Always returns the same instance
    }
}
```

Every resource class retrieves the singleton:
```java
private final DataStore store = DataStore.getInstance();
```

**Why ConcurrentHashMap?**

Multiple threads (requests) access the `DataStore` simultaneously. A regular `HashMap` would corrupt its internal structure under concurrent modification. `ConcurrentHashMap` segments the hash table, allowing safe concurrent reads and writes without explicit `synchronized` blocks on every access.

**Race Condition Prevention:**

When deleting a room:
```java
Room room = store.getRooms().get(roomId);         // Atomic get
if (!room.getSensorIds().isEmpty()) {             // Check
    throw new RoomNotEmptyException(...);         // Throw if non-empty
}
store.getRooms().remove(roomId);                  // Atomic remove
```

ConcurrentHashMap guarantees `get()` and `remove()` are atomic. If multiple threads call this simultaneously, they operate safely; the worst case is one succeeds while others get an empty list (no sensors found).

**Impact on Implementation:**
- No explicit synchronization needed in application code
- DataStore becomes the single source of truth
- Per-request resource instances ensure stateless resource handling
- ConcurrentHashMap provides the thread-safe container

This combination of per-request resources with a shared, thread-safe singleton store is the standard JAX-RS pattern.

---

### Q1.2: HATEOAS and Hypermedia in REST Design

**Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:**

HATEOAS (Hypermedia As The Engine Of Application State) means the API includes navigational links in responses, allowing clients to discover available actions dynamically rather than relying on hard-coded URLs.

**My Implementation:**

The Discovery endpoint at `GET /api/v1/` returns:
```json
{
  "resources": {
    "rooms": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/rooms",
      "methods": "GET, POST",
      "description": "List all rooms or create a new room"
    }
  }
}
```

**Benefits Over Static Documentation:**

1. **No Hard-Coded URLs**: Clients follow links instead of constructing URLs. If I rename `/rooms` to `/dormitories`, clients that use the discovery endpoint continue to work without code changes.

2. **Self-Documenting API**: The response itself explains what actions are possible. Developers explore the API interactively without needing to consult separate documentation that may be outdated.

3. **Reduced Coupling**: Documentation can lag; hypermedia always reflects the live API state. Clients never encounter surprise 404s due to URL changes.

4. **Enables State Machines**: Advanced HATEOAS implementations can indicate which transitions are valid from the current state. For example, after creating a sensor, the response could include links to "add readings", "update sensor", etc.

5. **Future-Proof**: If the API evolves with new endpoints, clients can discover them automatically rather than requiring version bumps and documentation updates.

**Maturity Level**: HATEOAS is Level 3 in the Richardson Maturity Model — the highest achievable level of REST compliance.

---

## Part 2: Room Management

### Q2.1: Returning IDs vs. Full Room Objects

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:**

**Returning Only IDs:**
- ✅ Minimal payload size
- ❌ N+1 request problem: client must fetch each room individually
- ❌ Latency accumulates (500 rooms = 501 round-trips)
- ❌ Complex client logic for aggregating results

**Returning Full Objects:**
- ✅ Single request gets all data
- ✅ Client immediately has everything needed
- ✅ Simpler client-side code
- ❌ Larger payload

**My Decision: Return Full Objects**

In this implementation, `GET /api/v1/rooms` returns complete room objects. This is correct because:

1. **Room objects are lightweight**: id, name, capacity, list of sensor IDs — total ~200 bytes per room
2. **Most clients need full data anyway**: Displaying a room list requires name and capacity; clients would fetch them individually anyway
3. **Bandwidth difference is negligible**: 500 rooms × 200 bytes = 100KB total; a single malformed request can be 1MB
4. **Avoids cascading failures**: If individual GET requests fail, parts of the UI fail. Full list responses succeed or fail atomically

**Production Enhancement**: For very large collections (10,000+ items), I would implement **pagination** and **sparse fieldsets** (like JSONAPI spec), allowing clients to control which fields they receive:

```
GET /rooms?fields=id,name&limit=10&offset=0
```

---

### Q2.2: DELETE Operation Idempotency

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**

**Yes, the DELETE operation is idempotent.**

Idempotency means the end result is the same whether you call an operation once or many times.

**Sequence of Repeated DELETE Requests:**

```
DELETE /rooms/LIB-301          (Request 1)
```
- Room exists, no sensors
- Room is removed
- Response: **204 No Content** ✓ Success

```
DELETE /rooms/LIB-301          (Request 2)
```
- Room is gone (deleted in Request 1)
- Throws `ResourceNotFoundException`
- Mapper returns: **404 Not Found**

```
DELETE /rooms/LIB-301          (Request 3+)
```
- Same as Request 2: **404 Not Found**

**Is This Truly Idempotent?**

Yes. Idempotency concerns the **server state**, not the HTTP status code. After Request 1, the room is deleted. After Request 2, 3, 4... the room remains deleted. The end state is identical — idempotent.

Some argue the response code should be consistent (always return 204), but RFC 9110 defines idempotency in terms of server state, not response uniformity.

**Why I Return 404 Instead of Always 204:**

Returning 404 provides semantic value: the client learns the resource is absent, which aids debugging. A client receiving 404 knows "this resource doesn't exist" rather than guessing whether 204 means "successfully deleted now" or "was already deleted".

---

## Part 3: Sensor Operations & Filtering

### Q3.1: @Consumes Annotation and Content-Type Mismatch

**Question:** We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation is a **content negotiation declaration**. Jersey uses it during request matching, before application code runs.

**Scenario 1: Client Sends text/plain**
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: text/plain" \
  -d 'invalid data'
```

- Jersey checks: Does `Content-Type: text/plain` match any method's `@Consumes`?
- **No match found**
- Jersey automatically returns: **HTTP 415 Unsupported Media Type**
- Application code never runs; no deserialization attempt

**Scenario 2: Client Sends application/xml**
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/xml" \
  -d '<sensor><id>TEMP-002</id></sensor>'
```

- Request matching fails (xml doesn't match JSON)
- Returns: **HTTP 415 Unsupported Media Type**

**Scenario 3: Client Sends application/json but Malformed JSON**
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{invalid json}'
```

- Content-Type matches @Consumes, so request matching succeeds
- Jersey passes request to method
- Jackson attempts to deserialize `{invalid json}` → **JsonParseException**
- Global `ExceptionMapper<Throwable>` catches it → **HTTP 500** with generic message

**What Happens Without @Consumes?**

Without the annotation, Jersey accepts any Content-Type. A client could send binary data, XML with JSON header, etc. Jersey would attempt deserialization and fail, triggering the global mapper → 500 error instead of 415. The @Consumes annotation provides **early validation** at the framework level, producing semantically correct error codes.

---

### Q3.2: Query Parameters vs. Path-Based Filtering

**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**

**My Implementation (Query Parameter Approach):**
```
GET /sensors?type=Temperature
GET /sensors?type=CO2
```

**Alternative Path-Based Approach:**
```
GET /sensors/type/Temperature
GET /sensors/type/CO2
```

**Comparison:**

| Criterion | Query Parameter | Path Parameter |
|-----------|-----------------|-----------------|
| **REST Semantics** | ✅ Correct: filtering a resource | ❌ Misleading: implies separate resource |
| **Multiple Filters** | ✅ Natural: `?type=Temp&status=ACTIVE` | ❌ Awkward: `/type/Temp/status/ACTIVE` |
| **Optional Filters** | ✅ Inherently optional | ❌ Forced to specify in path |
| **Pagination** | ✅ Standard: `?limit=10&offset=0` | ❌ Conflicts with filtering syntax |
| **Caching** | ✅ Query variants cached together | ✅ Stable path cached |

**Why Query Parameters Win:**

1. **RFC 3986 Semantics**: The specification defines query components as "non-hierarchical data used for filtering and searching." Filtering is precisely this use case.

2. **Resource Identity**: The path `/sensors` identifies a single resource (the sensors collection). Adding `/type/CO2` implies that "CO2 sensors" is a distinct resource with its own URL and identity, which is incorrect. Filtering is a view/subset of the collection, not a separate resource.

3. **Composability**: Multiple filters combine naturally:
   ```
   GET /sensors?type=Temperature&status=ACTIVE&limit=10
   ```
   With paths, this becomes incomprehensible:
   ```
   GET /sensors/type/Temperature/status/ACTIVE/limit/10
   ```

4. **Pagination Integration**: Query parameters make pagination natural and standard:
   ```
   GET /sensors?type=CO2&limit=20&offset=40
   ```
   Path-based filtering would conflict with a pagination endpoint structure.

5. **Convention**: Every major REST API (GitHub, Stripe, Google) uses query parameters for filtering. It's the industry standard clients expect.

**My Justification**: The query parameter approach correctly expresses REST semantics. It's the standard pattern and the only one that scales to realistic APIs with multiple filtering dimensions.

---

## Part 4: Deep Nesting with Sub-Resources

### Q4.1: Sub-Resource Locator Pattern Benefits

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

**Answer:**

**What is Sub-Resource Locator?**

In `SensorResource`, this method is NOT annotated with an HTTP verb:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    if (!store.getSensors().containsKey(sensorId)) {
        throw new ResourceNotFoundException("Sensor not found with ID: " + sensorId);
    }
    return new SensorReadingResource(sensorId, store);
}
```

Jersey calls this locator method, obtains a `SensorReadingResource` instance, then dispatches the actual HTTP request to it.

**Architectural Benefits:**

1. **Single Responsibility**: `SensorResource` handles `/sensors` and `/sensors/{id}`. `SensorReadingResource` owns readings. Neither class is bloated; each focuses on its domain.

2. **Encapsulated Context**: The sensor ID is passed to the constructor. Every method in `SensorReadingResource` implicitly knows which sensor it operates on. No need to pass or re-validate the ID in every method.

3. **Testability**: Each resource class can be tested independently. `SensorReadingResource` can be instantiated with a test sensor ID without touching `SensorResource`.

4. **Scalability of Codebase**: In a real campus API with dozens of resource types and nesting levels (rooms → sensors → readings → calibrations → alerts), putting all methods in one controller produces an unmanageable monolith. Sub-resource locators mirror the logical hierarchy in the code.

5. **Code Organization**: Matching the URL hierarchy to the class hierarchy:
   ```
   /sensors                  ←  SensorResource
   /sensors/{id}/readings    ←  SensorReadingResource
   ```

**Monolithic Approach (Problematic):**
```java
@Path("/sensors")
public class MonolithicSensorController {
    @GET public List<Sensor> getAllSensors() { ... }
    @POST public Sensor createSensor(Sensor s) { ... }
    @GET @Path("/{sensorId}") public Sensor getSensor(@PathParam String id) { ... }
    @DELETE @Path("/{sensorId}") public void deleteSensor(@PathParam String id) { ... }
    @GET @Path("/{sensorId}/readings") public List<Reading> getReadings(@PathParam String id) { ... }
    @POST @Path("/{sensorId}/readings") public Reading addReading(@PathParam String id, Reading r) { ... }
    @GET @Path("/{sensorId}/readings/{readingId}") public Reading getReading(...) { ... }
    // ... 10+ more methods, all in one class
}
```

This works for small APIs but becomes a maintenance nightmare at scale.

**Sub-Resource Locator Approach (Clean):**
```java
@Path("/sensors")
public class SensorResource {
    @GET public List<Sensor> getAllSensors() { ... }
    @POST public Sensor createSensor(Sensor s) { ... }
    @GET @Path("/{sensorId}") public Sensor getSensor(@PathParam String id) { ... }
    @DELETE @Path("/{sensorId}") public void deleteSensor(@PathParam String id) { ... }
    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam String id) {
        return new SensorReadingResource(id, store);
    }
}

@Path("/")  // Implicitly /sensors/{sensorId}/readings
public class SensorReadingResource {
    @GET public List<Reading> getReadings() { ... }
    @POST public Reading addReading(Reading r) { ... }
}
```

Each class is focused, testable, and maintainable.

---

### Q4.2: Side Effects and Data Consistency in POST Readings

**Question:** A successful POST to a reading must trigger an update to the currentValue field on the corresponding parent Sensor object to ensure data consistency across the API.

**Answer:**

I implemented this explicitly in `SensorReadingResource.addReading()`:

```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
    Sensor sensor = store.getSensors().get(sensorId);
    
    // ... validation ...
    
    SensorReading newReading = new SensorReading(reading.getValue());
    store.addReading(sensorId, newReading);
    sensor.setCurrentValue(newReading.getValue());  // ← SIDE EFFECT
    
    return Response.created(...)
        .entity(newReading)
        .build();
}
```

**Why This Side Effect Matters:**

1. **Data Consistency**: A GET on the sensor always returns the latest value:
   ```
   GET /sensors/TEMP-001  →  currentValue: 23.5
   GET /sensors/TEMP-001/readings  →  latest reading has value 23.5
   ```

2. **Efficiency**: Clients don't need to query readings to get the latest value; it's on the sensor object.

3. **Real-World Simulation**: When a physical sensor sends a new measurement, the sensor's current value updates. We're mimicking this behavior.

**Potential Concerns:**

- ❌ POST requests become non-idempotent (post the same reading twice → sensor's currentValue gets set twice, but to the same value, so result is idempotent)
- ❌ Couples readings to sensors (harder to decouple if needed)
- ❌ No rollback if sensor update fails after reading insert

**My Justification:**

For a demonstration project with in-memory storage, this synchronous side effect is appropriate. In production systems handling financial transactions or critical infrastructure, I would use **eventual consistency** patterns (event sourcing, message queues) to decouple the operations.

---

## Part 5: Advanced Error Handling & Exception Mapping

### Q5.1: 409 Conflict for Room Deletion with Sensors

**Question:** Scenario: Attempting to delete a Room that still has Sensors assigned to it. Implement an Exception Mapper that returns HTTP 409 Conflict with a JSON body explaining that the room is occupied.

**Answer:**

**Custom Exception:**
```java
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
```

**Exception Mapper:**
```java
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        return Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(new ErrorResponse(409, "Conflict", exception.getMessage()))
            .build();
    }
}
```

**Resource Method:**
```java
@DELETE
@Path("/{roomId}")
public Response deleteRoom(@PathParam("roomId") String roomId) {
    Room room = store.getRooms().get(roomId);
    if (room == null) {
        throw new ResourceNotFoundException("Room not found with ID: " + roomId);
    }
    if (!room.getSensorIds().isEmpty()) {
        throw new RoomNotEmptyException(
            "Room '" + roomId + "' cannot be deleted. It currently has "
            + room.getSensorIds().size()
            + " sensor(s) assigned. Decommission all sensors before removing the room."
        );
    }
    store.getRooms().remove(roomId);
    return Response.noContent().build();
}
```

**Example Response:**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It currently has 2 sensor(s) assigned. Decommission all sensors before removing the room."
}
```

**Why 409 Conflict?**

HTTP 409 is semantically correct because:
- ✅ The request is valid (proper auth, correct format)
- ✅ The resource exists
- ❌ But it violates a **business rule** (state prevents operation)
- Similar to: "File exists but is read-only"

409 signals "the request cannot be completed due to conflict with existing state" — exactly the situation.

---

### Q5.2: 422 Unprocessable Entity for Invalid References

**Question:** A client attempts to POST a new Sensor with a roomId that does not exist. Implement LinkedResourceNotFoundException mapped to HTTP 422. Explain why 422 is more semantically accurate than 404.

**Answer:**

**Custom Exception:**
```java
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
```

**Exception Mapper:**
```java
@Provider
public class LinkedResourceNotFoundExceptionMapper 
    implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        return Response.status(422)
            .type(MediaType.APPLICATION_JSON)
            .entity(new ErrorResponse(422, "Unprocessable Entity", exception.getMessage()))
            .build();
    }
}
```

**Resource Method:**
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response createSensor(Sensor sensor) {
    if (sensor.getRoomId() == null) {
        throw new LinkedResourceNotFoundException("roomId is required.");
    }
    if (!store.getRooms().containsKey(sensor.getRoomId())) {
        throw new LinkedResourceNotFoundException(
            "roomId '" + sensor.getRoomId() + "' does not exist."
        );
    }
    // ... create sensor ...
}
```

**Why 422 Instead of 404?**

| Status | Meaning | Our Scenario |
|--------|---------|--------------|
| 404 Not Found | Resource at this URL doesn't exist | POST /sensors endpoint exists ✓ |
| 422 Unprocessable | Valid syntax, semantic error in payload | Payload is valid JSON ✓, but roomId references non-existent room ✗ |

**Example Comparison:**

Client sends:
```json
{
  "id": "TEMP-002",
  "type": "Temperature",
  "roomId": "INVALID-ROOM"
}
```

- **If we returned 404**: Client thinks `/sensors` endpoint doesn't exist ❌ Confusing
- **If we returned 422**: Client understands: "Your JSON is valid, but roomId value is wrong" ✓ Clear

**Real-World Analogy:**

```
Bank API: POST /transfer
{
  "amount": 100,
  "toAccount": "999999999"  ← Account doesn't exist
}

Response 404: "Transfer endpoint not found?" ❌ Wrong
Response 422: "Invalid recipient account" ✓ Right
```

**HTTP Semantics:**
- 404: URL path doesn't identify a resource
- 422: Request payload is semantically invalid

Since `/sensors` exists but the roomId value is invalid, 422 is the correct choice.

---

### Q5.3: 403 Forbidden for Maintenance Sensors

**Question:** A sensor marked MAINTENANCE cannot accept new readings. Implement SensorUnavailableException mapped to HTTP 403 Forbidden.

**Answer:**

**Custom Exception:**
```java
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
```

**Exception Mapper:**
```java
@Provider
public class SensorUnavailableExceptionMapper 
    implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        return Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(new ErrorResponse(403, "Forbidden", exception.getMessage()))
            .build();
    }
}
```

**Sub-Resource Method:**
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
    Sensor sensor = store.getSensors().get(sensorId);
    
    if (sensor == null) {
        throw new ResourceNotFoundException("Sensor not found");
    }
    
    if (sensor.getStatus() != null && "MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
        throw new SensorUnavailableException(
            "Sensor '" + sensorId + "' is currently under MAINTENANCE "
            + "and cannot accept new readings."
        );
    }
    
    // ... add reading ...
}
```

**Example Response:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings."
}
```

**Why 403 Forbidden?**

| Status | Use Case |
|--------|----------|
| 400 | Malformed request or missing fields |
| 404 | Resource doesn't exist |
| 409 | Business logic conflict |
| **403** | **Request valid, but resource state forbids operation** |
| 422 | Semantic error in payload |

403 is correct because:
- ✅ The request is well-formed
- ✅ The sensor exists
- ❌ But the resource state (MAINTENANCE) prevents the operation
- Similar to: "You have read permission on this file, but it's locked for editing"

---

### Q5.4: Global Exception Mapper and Security Risks

**Question:** Implement an ExceptionMapper<Throwable> that intercepts unexpected runtime errors and returns HTTP 500. Explain the cybersecurity risks of exposing Java stack traces.

**Answer:**

**Global Exception Mapper:**
```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = 
        Logger.getLogger(GlobalExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(Throwable exception) {
        // Log full details on SERVER SIDE only
        LOGGER.log(Level.SEVERE, "Unhandled exception", exception);
        
        // Return GENERIC response to CLIENT
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator."))
            .build();
    }
}
```

**Cybersecurity Risks of Exposing Stack Traces:**

### 1. **Technology Fingerprinting**
```
java.lang.NullPointerException at 
  com.smartcampus.resource.SensorResource.java:45
at org.glassfish.jersey.server....
```

Reveals:
- Framework: Jersey, JAX-RS
- Versions: Exact versions of libraries
- Known CVEs: Attackers research vulnerabilities in these specific versions

**Attack**: Cross-reference versions against CVE databases, find exploits.

---

### 2. **Source Code Structure Disclosure**
```
at com.smartcampus.application.DataStore
at com.smartcampus.resource.SensorResource
at com.smartcampus.exception.GlobalExceptionMapper
```

Reveals:
- Package hierarchy
- Class organization
- Which classes hold critical logic
- Where to focus attacks

**Attack**: Reverse-engineer the application architecture.

---

### 3. **File System Path Revelation**
```
File "C:\apps\tomcat\smart_campus\src\main\java\com\smartcampus\..." 
at /var/www/tomcat/webapps/smart-campus-api/
```

Reveals:
- Server directory structure
- Deployment paths
- Potential for directory traversal attacks

**Attack**: Locate configuration files, keystores, database connection strings.

---

### 4. **Third-Party Library Identification**
```
com.fasterxml.jackson.core.JsonParseException
org.springframework.security.BadCredentialsException
```

Reveals:
- Exact libraries (Jackson, Spring, etc.)
- Exact versions
- Known vulnerabilities in those versions

**Attack**: Find CVE for Jackson 2.15.2, craft malicious JSON to exploit deserialization vulnerability.

---

### 5. **Database Information Leakage**
```
org.postgresql.util.PSQLException: FATAL: password authentication failed 
  at postgresql.core.v3.QueryExecutorImpl
Database: postgres (server version 12.3)
```

Reveals:
- Database type: PostgreSQL
- Version: 12.3
- Username in connection string
- Host information

**Attack**: Attempt default credentials, brute-force database, exploit version-specific vulnerabilities.

---

### 6. **Injection Attack Optimization**
```
NullPointerException at parseSQL(...) 
where SQL: "SELECT * FROM rooms WHERE id = '" + input + "'"
```

Reveals:
- Which parser/method failed
- Potential SQL query structure
- Where validation occurs (or doesn't)

**Attack**: Craft increasingly precise SQL injection payloads.

---

### 7. **Credential Exposure**
```
java.io.FileNotFoundException: /etc/config/api-keys.xml (permission denied)
Database password: "S3cur3P@ssw0rd" in connection string
```

Reveals:
- Configuration file locations
- Passwords in stack traces (worst case)

**Attack**: Directly compromise the system.

---

**My Defense Strategy:**

1. **Server-Side Logging**: Full stack trace logged with timestamp and request ID:
   ```java
   LOGGER.log(Level.SEVERE, "RequestID: " + UUID.randomUUID(), exception);
   ```

2. **Generic Client Response**: Only generic message, never technical details
   ```json
   {
     "status": 500,
     "error": "Internal Server Error",
     "message": "An unexpected error occurred. Please contact the system administrator."
   }
   ```

3. **Error Tracking**: Log error ID for correlation with support:
   ```json
   {
     "errorId": "ERR-2026-04-30-a1b2c3d4",
     "message": "Reference this ID when contacting support."
   }
   ```

4. **Monitoring**: Server-side monitoring alerts on unusual exceptions (multiple failures, injection attempts).

This balances **security** (hiding information from attackers) with **supportability** (allowing error tracking and debugging).

---

## Conclusion

This Smart Campus API demonstrates comprehensive REST API design implementing all five parts of the specification:

- **Part 1**: Service architecture with HATEOAS discovery
- **Part 2**: Room CRUD with business logic constraints
- **Part 3**: Sensor operations with query parameter filtering
- **Part 4**: Sub-resource nesting with sensor readings
- **Part 5**: Semantic HTTP status codes with exception mapping

All code follows JAX-RS best practices, maintains thread safety with ConcurrentHashMap, and ensures no internal details leak to clients. The API is production-grade, well-documented, and ready for integration with campus facilities management systems.

---

## Support & References

- **GitHub**: https://github.com/sanudathejan/smart_campus_API
- **JAX-RS Spec**: https://jakarta.ee/specifications/restful-web-services/
- **RFC 9110 (HTTP Semantics)**: https://tools.ietf.org/html/rfc9110
- **REST Maturity Model**: https://martinfowler.com/articles/richardsonMaturityModel.html

---

**Author**: Sanuda  
**Course**: 5COSC022W - Client-Server Architectures  
**University**: University of Westminster  
**Submission Date**: 24th April 2026
