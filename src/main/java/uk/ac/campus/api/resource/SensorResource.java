package uk.ac.campus.api.resource;

import uk.ac.campus.api.exception.InvalidReferenceException;
import uk.ac.campus.api.model.ErrorResponse;
import uk.ac.campus.api.model.Room;
import uk.ac.campus.api.model.Sensor;
import uk.ac.campus.api.store.InMemoryRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages the /api/v1/sensors collection.
 *
 * Endpoints:
 *   GET  /api/v1/sensors              — list all sensors (optional ?type= filter)
 *   POST /api/v1/sensors              — register a sensor (roomId must exist)
 *   GET  /api/v1/sensors/{id}         — fetch one sensor
 *   PUT  /api/v1/sensors/{id}         — update sensor status or type
 *   DELETE /api/v1/sensors/{id}       — remove a sensor
 *
 * Sub-resource locator:
 *   ANY  /api/v1/sensors/{id}/readings → delegates to ReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     *
     * Returns all sensors, optionally filtered by type.
     *
     * @QueryParam vs path-based filtering (answered in report):
     * URL path segments should identify resources, not describe search criteria.
     * Using /sensors/type/CO2 would pollute the routing table with a dedicated
     * path for every possible filter value or combination. @QueryParam keeps
     * the collection URL clean (/sensors) and makes filters optional and
     * composable (e.g. ?type=CO2&status=ACTIVE) without creating new routes.
     * Query parameters are the REST convention for filtering, sorting, and
     * pagination of collections.
     */
    @GET
    public Response listSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(InMemoryRegistry.sensors().values());

        if (type != null && !type.trim().isEmpty()) {
            result = result.stream()
                    .filter(s -> s.getType() != null
                            && s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(result).build();
    }

    /**
     * POST /api/v1/sensors
     *
     * Registers a new sensor. The roomId in the body MUST reference an
     * existing room — otherwise InvalidReferenceException (→ 422) is thrown.
     *
     * @Consumes(APPLICATION_JSON) consequences (answered in report):
     * Jersey inspects the Content-Type header of every incoming request and
     * matches it against the @Consumes annotation before invoking the method.
     * If a client sends text/plain or application/xml, Jersey returns HTTP 415
     * Unsupported Media Type immediately — the resource method is never called.
     * This prevents malformed or unexpected payloads from reaching application
     * logic and producing cryptic errors.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {
        if (sensor == null) {
            return badRequest("Request body is required.");
        }
        if (blank(sensor.getType())) {
            return badRequest("Field 'type' is required (e.g. Temperature, CO2, Occupancy).");
        }
        if (blank(sensor.getRoomId())) {
            return badRequest("Field 'roomId' is required.");
        }

        // Referential integrity check — roomId must point to a real room
        Room parentRoom = InMemoryRegistry.findRoom(sensor.getRoomId());
        if (parentRoom == null) {
            throw new InvalidReferenceException("roomId", sensor.getRoomId());
        }

        if (blank(sensor.getId())) {
            sensor.setId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (InMemoryRegistry.sensorExists(sensor.getId())) {
            ErrorResponse err = new ErrorResponse(409, "Conflict",
                    "A sensor with id '" + sensor.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }

        // Default to ACTIVE if status not supplied
        if (blank(sensor.getStatus())) sensor.setStatus("ACTIVE");

        String normalised = sensor.getStatus().toUpperCase();
        if (!normalised.equals("ACTIVE") && !normalised.equals("MAINTENANCE")
                && !normalised.equals("OFFLINE")) {
            return badRequest("Field 'status' must be one of: ACTIVE, MAINTENANCE, OFFLINE.");
        }
        sensor.setStatus(normalised);

        InMemoryRegistry.saveSensor(sensor);

        // Side-effect: register sensor ID inside the parent room's list
        parentRoom.attachSensor(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns full details for one sensor. 404 if not found.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = InMemoryRegistry.findSensor(sensorId);
        if (sensor == null) {
            return notFound("No sensor found with id '" + sensorId + "'.");
        }
        return Response.ok(sensor).build();
    }

    /**
     * PUT /api/v1/sensors/{sensorId}
     * Updates mutable fields (status, type) of an existing sensor.
     */
    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor update) {
        Sensor existing = InMemoryRegistry.findSensor(sensorId);
        if (existing == null) {
            return notFound("No sensor found with id '" + sensorId + "'.");
        }

        if (!blank(update.getStatus())) {
            String s = update.getStatus().toUpperCase();
            if (!s.equals("ACTIVE") && !s.equals("MAINTENANCE") && !s.equals("OFFLINE")) {
                return badRequest("Field 'status' must be one of: ACTIVE, MAINTENANCE, OFFLINE.");
            }
            existing.setStatus(s);
        }

        if (!blank(update.getType())) {
            existing.setType(update.getType());
        }

        return Response.ok(existing).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Removes a sensor and unlinks it from its parent room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response removeSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = InMemoryRegistry.findSensor(sensorId);
        if (sensor == null) {
            return notFound("Sensor '" + sensorId + "' does not exist or was already removed.");
        }

        InMemoryRegistry.deleteSensor(sensorId);

        // Clean up the parent room's sensor list
        Room parentRoom = InMemoryRegistry.findRoom(sensor.getRoomId());
        if (parentRoom != null) parentRoom.detachSensor(sensorId);

        ErrorResponse confirmation = new ErrorResponse(200, "OK",
                "Sensor '" + sensorId + "' removed successfully.");
        return Response.ok(confirmation).build();
    }

    /**
     * Sub-resource locator — /api/v1/sensors/{sensorId}/readings
     *
     * No HTTP method annotation here — this method acts purely as a locator.
     * Jersey calls it to obtain the object that will handle the /readings path,
     * then dispatches the actual HTTP verb to ReadingResource.
     *
     * Sub-resource locator benefits (answered in report):
     * Defining every nested path in one controller creates a "God class" that
     * is hard to read, test, and maintain. Delegating /readings to a dedicated
     * ReadingResource applies the Single Responsibility Principle: SensorResource
     * handles sensor metadata, ReadingResource handles historical data. Each
     * class is smaller, independently testable, and easier to extend without
     * risking regressions in unrelated functionality.
     */
    @Path("/{sensorId}/readings")
    public ReadingResource routeToReadings(@PathParam("sensorId") String sensorId) {
        return new ReadingResource(sensorId);
    }

    // ── private helpers ────────────────────────────────────────────────────

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(400, "Bad Request", message))
                .build();
    }

    private Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(404, "Not Found", message))
                .build();
    }

    private boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
