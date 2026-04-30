# Smart Campus API - Complete Testing Summary

## BASE URL
```
http://localhost:8080/smart-campus-api/api/v1
```

---

## ALL ENDPOINTS SUMMARY TABLE

### Part 1: Discovery (1 endpoint)
| # | Method | Endpoint | Expected Status | Notes |
|---|--------|----------|-----------------|-------|
| 1.1 | GET | `/` | 200 | Returns API metadata |

---

### Part 2: Room Management (7 endpoints)
| # | Method | Endpoint | Expected Status | Request Body | Notes |
|---|--------|----------|-----------------|---------------|-------|
| 2.1 | GET | `/rooms` | 200 | - | Get all rooms |
| 2.2 | POST | `/rooms` | 201 | `{id, name, capacity}` | Create new room |
| 2.3 | GET | `/rooms/{roomId}` | 200 | - | Get specific room |
| 2.4 | GET | `/rooms/NO-ROOM` | 404 | - | Room not found error |
| 2.5 | DELETE | `/rooms/{empty-room}` | 204 | - | Delete room with no sensors |
| 2.6 | DELETE | `/rooms/LIB-301` | 409 | - | Conflict: room has sensors |
| 2.7 | POST | `/rooms` (missing id) | 400 | `{name, capacity}` | Bad request: missing field |

---

### Part 3: Sensor Operations (8 endpoints)
| # | Method | Endpoint | Expected Status | Request Body | Notes |
|---|--------|----------|-----------------|---------------|-------|
| 3.1 | GET | `/sensors` | 200 | - | Get all sensors |
| 3.2 | GET | `/sensors?type=Temperature` | 200 | - | Filter by type |
| 3.3 | GET | `/sensors?type=CO2` | 200 | - | Filter by type |
| 3.4 | GET | `/sensors/{sensorId}` | 200 | - | Get specific sensor |
| 3.5 | GET | `/sensors/INVALID` | 404 | - | Sensor not found |
| 3.6 | POST | `/sensors` | 201 | `{id, type, roomId}` | Create sensor |
| 3.7 | POST | `/sensors` | 422 | `{id, type, roomId: INVALID}` | Invalid roomId |
| 3.8 | POST | `/sensors` | 400 | `{type, roomId}` | Missing id field |

---

### Part 4: Sensor Readings/Sub-Resources (5 endpoints)
| # | Method | Endpoint | Expected Status | Request Body | Notes |
|---|--------|----------|-----------------|---------------|-------|
| 4.1 | GET | `/sensors/{sensorId}/readings` | 200 | - | Get all readings |
| 4.2 | POST | `/sensors/{sensorId}/readings` | 201 | `{value: 23.5}` | Add reading |
| 4.3 | POST | `/sensors/OCC-001/readings` | 403 | `{value: 10}` | Forbidden (maintenance) |
| 4.4 | GET | `/sensors/INVALID/readings` | 404 | - | Sensor not found |
| 4.5 | POST | `/sensors/INVALID/readings` | 404 | `{value: 10}` | Sensor not found |

---

### Part 5: Error Handling (4 endpoints)
| # | Method | Endpoint | Expected Status | Error Type | Notes |
|---|--------|----------|-----------------|-----------|-------|
| 5.1 | DELETE | `/rooms/LIB-301` | 409 | Conflict | Room has sensors |
| 5.2 | POST | `/sensors` | 422 | Unprocessable | Invalid roomId reference |
| 5.3 | POST | `/sensors/OCC-001/readings` | 403 | Forbidden | Sensor in maintenance |
| 5.4 | GET | `/rooms/NO-ROOM` | 404 | Not Found | Resource doesn't exist |

---

## COMPLETE CURL COMMANDS

### Discovery
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/" \
  -H "Accept: application/json"
```

### Room Management
```bash
# Get all rooms
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Accept: application/json"

# Create room
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Workshop","capacity":40}'

# Get single room
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301" \
  -H "Accept: application/json"

# Get non-existent room (404)
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/rooms/NO-ROOM" \
  -H "Accept: application/json"

# Delete empty room (204)
curl -X DELETE "http://localhost:8080/smart-campus-api/api/v1/rooms/ENG-201"

# Delete room with sensors (409)
curl -X DELETE "http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301"

# Create room missing id (400)
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Room"}'
```

### Sensor Operations
```bash
# Get all sensors
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors" \
  -H "Accept: application/json"

# Filter sensors by type
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"

curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2" \
  -H "Accept: application/json"

# Get specific sensor
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001" \
  -H "Accept: application/json"

# Get non-existent sensor (404)
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors/INVALID" \
  -H "Accept: application/json"

# Create sensor
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d '{"id":"LIGHT-001","type":"Lighting","roomId":"LIB-301"}'

# Create sensor with invalid roomId (422)
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","roomId":"INVALID-ROOM"}'

# Create sensor missing id (400)
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d '{"type":"Temperature","roomId":"LIB-301"}'
```

### Sensor Readings
```bash
# Get all readings for sensor
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings" \
  -H "Accept: application/json"

# Add reading
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings" \
  -H "Content-Type: application/json" \
  -d '{"value":23.5}'

# Add reading to maintenance sensor (403)
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings" \
  -H "Content-Type: application/json" \
  -d '{"value":10}'

# Get readings from non-existent sensor (404)
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors/INVALID/readings" \
  -H "Accept: application/json"
```

---

## SEED DATA IN DATABASE

### Pre-existing Rooms:
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

### Pre-existing Sensors:
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

---

## RESPONSE STATUS CODES

| Code | Meaning | Example |
|------|---------|---------|
| **200** | OK - Successful GET request | GET `/sensors` |
| **201** | Created - Successful POST request | POST `/rooms` |
| **204** | No Content - Successful DELETE | DELETE `/rooms/{empty}` |
| **400** | Bad Request - Missing required fields | POST with missing `id` |
| **404** | Not Found - Resource doesn't exist | GET `/rooms/NO-ROOM` |
| **409** | Conflict - Business logic violation | DELETE room with sensors |
| **422** | Unprocessable Entity - Invalid reference | POST sensor with invalid roomId |
| **403** | Forbidden - State constraint violation | POST reading to maintenance sensor |
| **500** | Internal Server Error - Unexpected error | Any unhandled exception |

---

## REQUIRED HEADERS

### For GET Requests:
```
Accept: application/json
```

### For POST/PUT Requests:
```
Content-Type: application/json
Accept: application/json
```

### For DELETE Requests:
```
(No special headers required)
```

---

## POSTMAN IMPORT STEPS

1. Open Postman
2. Click **Import** button
3. Select **Upload Files**
4. Choose `smart-campus-api-postman-collection.json`
5. Click **Import**
6. All test cases will be available in the collection

---

## QUICK TESTING WORKFLOW

1. **Start with Discovery**
   - GET `/` → Verify API is running

2. **Test Room Management**
   - GET `/rooms` → List existing rooms
   - POST `/rooms` → Create new room (ENG-201)
   - GET `/rooms/LIB-301` → Get specific room
   - DELETE `/rooms/LIB-301` → Expect 409 conflict

3. **Test Sensor Operations**
   - GET `/sensors` → List all sensors
   - GET `/sensors?type=Temperature` → Filter sensors
   - POST `/sensors` → Create new sensor
   - POST `/sensors` with invalid roomId → Expect 422

4. **Test Sensor Readings**
   - GET `/sensors/TEMP-001/readings` → Get readings
   - POST `/sensors/TEMP-001/readings` → Add reading
   - POST `/sensors/OCC-001/readings` → Expect 403 (maintenance)

5. **Test Error Handling**
   - GET `/rooms/NO-ROOM` → Expect 404
   - DELETE `/rooms/LIB-301` → Expect 409
   - POST `/sensors` with invalid roomId → Expect 422
   - POST reading to maintenance sensor → Expect 403

---

## EXPECTED RESPONSE EXAMPLES

### 200 OK Response
```json
{
  "status": 200,
  "data": [...]
}
```

### 201 Created Response
```json
{
  "id": "new-id",
  "name": "New Item",
  ...
}
```

### 400 Bad Request
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Room 'id' field is required."
}
```

### 404 Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found with ID: NO-ROOM"
}
```

### 409 Conflict
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It currently has 2 sensor(s) assigned."
}
```

### 422 Unprocessable Entity
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId 'INVALID-ROOM' does not exist."
}
```

### 403 Forbidden
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings."
}
```

---

## TOTAL TEST CASES: 28

- **Part 1 (Discovery):** 1 test
- **Part 2 (Room Management):** 7 tests  
- **Part 3 (Sensor Operations):** 8 tests
- **Part 4 (Sensor Readings):** 5 tests
- **Part 5 (Error Handling):** 4 tests
- **Flexible Tests:** 3 additional variations

---

## FILES PROVIDED

1. **POSTMAN_TEST_GUIDE.md** - Detailed test guide with expected outputs
2. **POSTMAN_QUICK_REFERENCE.md** - Quick reference table with all endpoints
3. **smart-campus-api-postman-collection.json** - Ready-to-import Postman collection
4. **COMPLETE_TESTING_SUMMARY.md** - This file with all curl commands

---

## NOTES

✅ All timestamps are in milliseconds (epoch time)  
✅ Sensor readings generate UUID automatically  
✅ Sensors default to "ACTIVE" if status not provided  
✅ OCC-001 is pre-set to "MAINTENANCE" status  
✅ Rooms with sensors cannot be deleted  
✅ All responses are JSON format  
✅ Base URL must include `/smart-campus-api/` context path
