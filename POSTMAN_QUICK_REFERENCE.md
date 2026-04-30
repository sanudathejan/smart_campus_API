# Smart Campus API - Quick URL Reference

## BASE URL: http://localhost:8080/smart-campus-api/api/v1

---

## DISCOVERY ENDPOINT
| Method | URL | Status | Notes |
|--------|-----|--------|-------|
| GET | `/` | 200 | Returns API metadata with resource links |

---

## ROOM MANAGEMENT
| Method | URL | Status | Body/Notes |
|--------|-----|--------|-----------|
| GET | `/rooms` | 200 | Get all rooms |
| POST | `/rooms` | 201 | Create room: `{id, name, capacity}` |
| GET | `/rooms/{roomId}` | 200 | Get specific room |
| GET | `/rooms/INVALID` | 404 | Room not found |
| DELETE | `/rooms/{roomId}` | 204 | Delete empty room |
| DELETE | `/rooms/{roomId}` | 409 | Room has sensors - CONFLICT |
| POST | `/rooms` | 400 | Missing id/name field |

---

## SENSOR OPERATIONS
| Method | URL | Status | Body/Notes |
|--------|-----|--------|-----------|
| GET | `/sensors` | 200 | Get all sensors |
| GET | `/sensors?type=Temperature` | 200 | Filter by type |
| GET | `/sensors?type=CO2` | 200 | Filter by type CO2 |
| GET | `/sensors?type=Occupancy` | 200 | Filter by type Occupancy |
| GET | `/sensors/{sensorId}` | 200 | Get specific sensor |
| GET | `/sensors/INVALID` | 404 | Sensor not found |
| POST | `/sensors` | 201 | Create sensor: `{id, type, roomId, status?}` |
| POST | `/sensors` | 422 | Invalid roomId - UNPROCESSABLE |
| POST | `/sensors` | 400 | Missing id/type field |

---

## SENSOR READINGS (Sub-Resources)
| Method | URL | Status | Body/Notes |
|--------|-----|--------|-----------|
| GET | `/sensors/{sensorId}/readings` | 200 | Get all readings for sensor |
| POST | `/sensors/{sensorId}/readings` | 201 | Add reading: `{value: 23.5}` |
| POST | `/sensors/{sensorId}/readings` | 403 | Sensor in MAINTENANCE - FORBIDDEN |
| GET | `/sensors/INVALID/readings` | 404 | Sensor not found |
| POST | `/sensors/INVALID/readings` | 404 | Sensor not found |

---

## ERROR STATUS CODES SUMMARY

| Code | Scenario | Example URL |
|------|----------|-------------|
| **200** | Successful GET | GET `/sensors` |
| **201** | Successful POST | POST `/rooms` |
| **204** | Successful DELETE (no content) | DELETE `/rooms/{empty-room}` |
| **400** | Bad Request (missing fields) | POST `/rooms` without id |
| **404** | Not Found | GET `/rooms/NO-ROOM` |
| **409** | Conflict (room has sensors) | DELETE `/rooms/LIB-301` |
| **422** | Invalid foreign key | POST `/sensors` with invalid roomId |
| **403** | Forbidden (maintenance status) | POST `/sensors/OCC-001/readings` |
| **500** | Internal Server Error | Unexpected error |

---

## SEEDED DATA (Pre-populated)

### Rooms:
- **LIB-301**: Library Quiet Study, capacity 50, has sensors [TEMP-001, CO2-001]
- **LAB-102**: Computer Science Lab, capacity 30, has sensor [OCC-001]

### Sensors:
- **TEMP-001**: Temperature, ACTIVE, value 22.5, in LIB-301
- **CO2-001**: CO2, ACTIVE, value 412.0, in LIB-301
- **OCC-001**: Occupancy, MAINTENANCE, value 0.0, in LAB-102

---

## TESTING CHECKLIST

### Part 1: Discovery (1 test)
- [ ] GET `/`

### Part 2: Room Management (8 tests)
- [ ] GET `/rooms`
- [ ] POST `/rooms` (create)
- [ ] GET `/rooms/LIB-301`
- [ ] GET `/rooms/NO-ROOM` (404)
- [ ] DELETE `/rooms/{empty}` (204)
- [ ] DELETE `/rooms/LIB-301` (409)
- [ ] POST `/rooms` missing id (400)
- [ ] POST `/rooms` missing name (400)

### Part 3: Sensor Operations (9 tests)
- [ ] GET `/sensors`
- [ ] GET `/sensors?type=Temperature`
- [ ] GET `/sensors?type=CO2`
- [ ] GET `/sensors/TEMP-001`
- [ ] GET `/sensors/INVALID` (404)
- [ ] POST `/sensors` (create)
- [ ] POST `/sensors` invalid roomId (422)
- [ ] POST `/sensors` missing id (400)
- [ ] POST `/sensors` missing type (400)

### Part 4: Sub-Resources (5 tests)
- [ ] GET `/sensors/TEMP-001/readings`
- [ ] POST `/sensors/TEMP-001/readings` (add reading)
- [ ] GET `/sensors/TEMP-001/readings` (verify reading added)
- [ ] GET `/sensors/INVALID/readings` (404)
- [ ] POST `/sensors/OCC-001/readings` (403 - in maintenance)

### Part 5: Error Handling (5 tests)
- [ ] DELETE `/rooms/LIB-301` (409 Conflict)
- [ ] POST `/sensors` invalid roomId (422 Unprocessable)
- [ ] POST `/sensors/OCC-001/readings` (403 Forbidden)
- [ ] GET `/rooms/NO-ROOM` (404 Not Found)
- [ ] Test global error handler (500)

---

## USEFUL CURL COMMANDS

```bash
# Discovery
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/" \
  -H "Accept: application/json"

# Get all rooms
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Accept: application/json"

# Create room
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/rooms" \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Workshop","capacity":40}'

# Get all sensors
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors" \
  -H "Accept: application/json"

# Filter sensors by type
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"

# Create sensor
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors" \
  -H "Content-Type: application/json" \
  -d '{"id":"LIGHT-001","type":"Lighting","roomId":"LIB-301"}'

# Get sensor readings
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings" \
  -H "Accept: application/json"

# Add reading
curl -X POST "http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings" \
  -H "Content-Type: application/json" \
  -d '{"value":23.5}'

# Delete room (expect 409 conflict)
curl -X DELETE "http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301"
```

---

## Notes:
- All timestamps are in epoch milliseconds
- Sensor readings get auto-generated UUIDs
- roomId is required for sensors and must exist
- Sensors default to "ACTIVE" status if not specified
- OCC-001 is in MAINTENANCE status and cannot accept readings
- Rooms with sensors cannot be deleted (409 Conflict)
