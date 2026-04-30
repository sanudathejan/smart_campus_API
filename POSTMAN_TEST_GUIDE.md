# Smart Campus API - Postman Testing Guide

## Base URL
```
http://localhost:8080/smart-campus-api/api/v1
```

---

## PART 1: DISCOVERY ENDPOINT (10 Marks)

### 1.1 Get API Metadata
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/`

**Expected Status:** 200 OK  
**Expected Output:**
```json
{
  "api": "Smart Campus Sensor and Room Management API",
  "version": "1.0.0",
  "description": "RESTful API for managing campus rooms and IoT sensors.",
  "timestamp": "2026-04-30T...",
  "contact": {
    "name": "Campus Facilities Team",
    "email": "admin@smartcampus.ac.uk",
    "department": "Estates and Facilities Management"
  },
  "resources": {
    "rooms": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/rooms",
      "methods": "GET, POST",
      "description": "List all rooms or create a new room"
    },
    "room": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/rooms/{roomId}",
      "methods": "GET, DELETE",
      "description": "Retrieve or decommission a specific room"
    },
    "sensors": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/sensors",
      "methods": "GET, POST",
      "description": "List sensors (supports ?type= filter) or register a new sensor"
    },
    "sensor": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/sensors/{sensorId}",
      "methods": "GET",
      "description": "Retrieve a specific sensor by ID"
    },
    "readings": {
      "href": "http://localhost:8080/smart-campus-api/api/v1/sensors/{sensorId}/readings",
      "methods": "GET, POST",
      "description": "Get or append historical readings for a sensor"
    }
  }
}
```

---

## PART 2: ROOM MANAGEMENT (20 Marks)

### 2.1 Get All Rooms
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms`

**Expected Status:** 200 OK  
**Expected Output:**
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

### 2.2 Create a New Room
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms`

**Request Body:**
```json
{
  "id": "ENG-201",
  "name": "Engineering Workshop",
  "capacity": 40
}
```

**Expected Status:** 201 Created  
**Expected Output:**
```json
{
  "id": "ENG-201",
  "name": "Engineering Workshop",
  "capacity": 40,
  "sensorIds": []
}
```

### 2.3 Get Specific Room (Existing)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301`

**Expected Status:** 200 OK  
**Expected Output:**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": ["TEMP-001", "CO2-001"]
}
```

### 2.4 Get Specific Room (Non-existent)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms/NO-ROOM`

**Expected Status:** 404 Not Found  
**Expected Output:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found with ID: NO-ROOM"
}
```

### 2.5 Delete Room with No Sensors (Success)
**Method:** DELETE  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms/ENG-201`  
(Assuming ENG-201 was created and has no sensors)

**Expected Status:** 204 No Content  
**Expected Output:** (Empty body)

### 2.6 Delete Room with Sensors (Conflict)
**Method:** DELETE  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301`

**Expected Status:** 409 Conflict  
**Expected Output:**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It currently has 2 sensor(s) assigned. Decommission all sensors before removing the room."
}
```

### 2.7 Create Room - Missing ID
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms`

**Request Body:**
```json
{
  "name": "Test Room"
}
```

**Expected Status:** 400 Bad Request  
**Expected Output:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Room 'id' field is required."
}
```

### 2.8 Create Room - Missing Name
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms`

**Request Body:**
```json
{
  "id": "TEST-001"
}
```

**Expected Status:** 400 Bad Request  
**Expected Output:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Room 'name' field is required."
}
```

---

## PART 3: SENSOR OPERATIONS & LINKING (20 Marks)

### 3.1 Get All Sensors
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors`

**Expected Status:** 200 OK  
**Expected Output:**
```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  },
  {
    "id": "CO2-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 412.0,
    "roomId": "LIB-301"
  },
  {
    "id": "OCC-001",
    "type": "Occupancy",
    "status": "MAINTENANCE",
    "currentValue": 0.0,
    "roomId": "LAB-102"
  }
]
```

### 3.2 Get Sensors with Type Filter
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature`

**Expected Status:** 200 OK  
**Expected Output:**
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

### 3.3 Get Sensors with Type Filter (CO2)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2`

**Expected Status:** 200 OK  
**Expected Output:**
```json
[
  {
    "id": "CO2-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 412.0,
    "roomId": "LIB-301"
  }
]
```

### 3.4 Get Specific Sensor (Existing)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001`

**Expected Status:** 200 OK  
**Expected Output:**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "LIB-301"
}
```

### 3.5 Get Specific Sensor (Non-existent)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-ENG-201`

**Expected Status:** 404 Not Found  
**Expected Output:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sensor not found with ID: TEMP-ENG-201"
}
```

### 3.6 Create New Sensor (Success)
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors`

**Request Body:**
```json
{
  "id": "LIGHT-001",
  "type": "Lighting",
  "roomId": "LIB-301",
  "status": "ACTIVE"
}
```

**Expected Status:** 201 Created  
**Expected Output:**
```json
{
  "id": "LIGHT-001",
  "type": "Lighting",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

### 3.7 Create Sensor - Invalid Room ID (422)
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors`

**Request Body:**
```json
{
  "id": "TEMP-002",
  "type": "Temperature",
  "roomId": "INVALID-ROOM"
}
```

**Expected Status:** 422 Unprocessable Entity  
**Expected Output:**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId 'INVALID-ROOM' does not exist. Register the room first before assigning sensors to it."
}
```

### 3.8 Create Sensor - Missing ID
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors`

**Request Body:**
```json
{
  "type": "Temperature",
  "roomId": "LIB-301"
}
```

**Expected Status:** 400 Bad Request  
**Expected Output:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Sensor 'id' field is required."
}
```

### 3.9 Create Sensor - Missing Type
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors`

**Request Body:**
```json
{
  "id": "TEMP-003",
  "roomId": "LIB-301"
}
```

**Expected Status:** 400 Bad Request  
**Expected Output:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Sensor 'type' field is required."
}
```

---

## PART 4: DEEP NESTING WITH SUB-RESOURCES (20 Marks)

### 4.1 Get Sensor Readings (Empty List)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings`

**Expected Status:** 200 OK  
**Expected Output:**
```json
[]
```

### 4.2 Add New Reading to Sensor
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings`

**Request Body:**
```json
{
  "value": 23.5
}
```

**Expected Status:** 201 Created  
**Expected Output:**
```json
{
  "id": "uuid-string-here",
  "timestamp": 1714521600000,
  "value": 23.5
}
```

### 4.3 Get Sensor Readings (With Data)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings`

**Expected Status:** 200 OK  
**Expected Output:**
```json
[
  {
    "id": "uuid-string-here",
    "timestamp": 1714521600000,
    "value": 23.5
  }
]
```

### 4.4 Add Multiple Readings
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings`

**Request Body:**
```json
{
  "value": 24.0
}
```

**Expected Status:** 201 Created  
**Expected Output:**
```json
{
  "id": "uuid-string-here",
  "timestamp": 1714521700000,
  "value": 24.0
}
```

### 4.5 Add Reading to Non-existent Sensor
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/INVALID-SENSOR/readings`

**Request Body:**
```json
{
  "value": 25.0
}
```

**Expected Status:** 404 Not Found  
**Expected Output:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Sensor not found with ID: INVALID-SENSOR"
}
```

---

## PART 5: ERROR HANDLING & EXCEPTION MAPPING (30 Marks)

### 5.1 Resource Conflict - Delete Room with Sensors (409)
**Method:** DELETE  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301`

**Expected Status:** 409 Conflict  
**Expected Output:**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It currently has 2 sensor(s) assigned. Decommission all sensors before removing the room."
}
```

### 5.2 Dependency Validation - Invalid Room ID (422)
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors`

**Request Body:**
```json
{
  "id": "TEST-SENSOR",
  "type": "Temperature",
  "roomId": "NONEXISTENT"
}
```

**Expected Status:** 422 Unprocessable Entity  
**Expected Output:**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId 'NONEXISTENT' does not exist. Register the room first before assigning sensors to it."
}
```

### 5.3 State Constraint - Sensor in Maintenance (403)
**Method:** POST  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings`

**Request Body:**
```json
{
  "value": 10
}
```

**Expected Status:** 403 Forbidden  
**Expected Output:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings."
}
```

### 5.4 Resource Not Found (404)
**Method:** GET  
**URL:** `http://localhost:8080/smart-campus-api/api/v1/rooms/NONEXISTENT`

**Expected Status:** 404 Not Found  
**Expected Output:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found with ID: NONEXISTENT"
}
```

### 5.5 Global Error Handler (500)
To test this, you can make any request with invalid JSON or try to trigger an unexpected error.

**Expected Status:** 500 Internal Server Error  
**Expected Output:**
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact the system administrator."
}
```

---

## POSTMAN COLLECTION - QUICK REFERENCE

### Step-by-Step Testing Order

1. **Discovery** → GET `/api/v1/`
2. **List Rooms** → GET `/api/v1/rooms`
3. **Get Single Room** → GET `/api/v1/rooms/LIB-301`
4. **Create Room** → POST `/api/v1/rooms`
5. **List Sensors** → GET `/api/v1/sensors`
6. **Filter Sensors** → GET `/api/v1/sensors?type=Temperature`
7. **Get Single Sensor** → GET `/api/v1/sensors/TEMP-001`
8. **Create Sensor** → POST `/api/v1/sensors`
9. **Get Readings** → GET `/api/v1/sensors/TEMP-001/readings`
10. **Add Reading** → POST `/api/v1/sensors/TEMP-001/readings`
11. **Delete Room** → DELETE `/api/v1/rooms/LIB-301` (expect 409)
12. **Test Error Cases** → All 4xx and 5xx status codes

---

## Headers for All Requests

```
Content-Type: application/json
Accept: application/json
```

---

## Notes

- **Base URL:** `http://localhost:8080/smart-campus-api/api/v1`
- **Content Type:** All requests and responses use JSON
- **Status Codes:**
  - 200 OK: Successful GET
  - 201 Created: Successful POST
  - 204 No Content: Successful DELETE
  - 400 Bad Request: Missing required fields
  - 404 Not Found: Resource doesn't exist
  - 409 Conflict: Business logic violation
  - 422 Unprocessable Entity: Invalid foreign key reference
  - 403 Forbidden: State constraint violation
  - 500 Internal Server Error: Unexpected server error

