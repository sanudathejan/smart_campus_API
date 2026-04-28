package uk.ac.campus.api.resource;

import uk.ac.campus.api.exception.SensorOfflineException;
import uk.ac.campus.api.model.ErrorResponse;
import uk.ac.campus.api.model.Reading;
import uk.ac.campus.api.model.Sensor;
import uk.ac.campus.api.store.InMemoryRegistry;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource for reading history — /api/v1/sensors/{sensorId}/readings
 *
 * This class is NOT annotated with @Path at class level because it is
 * instantiated via the sub-resource locator in SensorResource, not by
 * Jersey's auto-discovery scanner. The URL context is inherited from the
 * parent locator method.
 *
 * Endpoints:
 *   GET  /api/v1/sensors/{sensorId}/readings  — full reading history
 *   POST /api/v1/sensors/{sensorId}/readings  — log a new reading
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingResource {

    private final String sensorId;

    /**
     * Instantiated by SensorResource.routeToReadings() with the sensor ID
     * extracted from the URL path.
     */
    public ReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the complete ordered reading history for the specified sensor.
     * Returns 404 if the parent sensor does not exist.
     */
    @GET
    public Response getHistory() {
        Sensor sensor = InMemoryRegistry.findSensor(sensorId);
        if (sensor == null) {
            ErrorResponse err = new ErrorResponse(404, "Not Found",
                    "No sensor found with id '" + sensorId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        List<Reading> history = InMemoryRegistry.readingsFor(sensorId);
        return Response.ok(history).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     *
     * Appends a new reading to the sensor's history log.
     *
     * Validations:
     *   1. Sensor must exist → 404
     *   2. Sensor must be ACTIVE → throws SensorOfflineException → 403
     *   3. Reading body must be present
     *
     * Side-effect (data consistency):
     *   On success, the parent sensor's latestReading field is updated to
     *   reflect the value just recorded, keeping the sensor's live state
     *   consistent with its most recent measurement.
     *
     * Auto-generation:
     *   id        → UUID if not provided
     *   capturedAt → current epoch ms if not provided or zero
     */
    @POST
    public Response addReading(Reading reading) {
        Sensor sensor = InMemoryRegistry.findSensor(sensorId);
        if (sensor == null) {
            ErrorResponse err = new ErrorResponse(404, "Not Found",
                    "No sensor found with id '" + sensorId + "'.");
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }

        // State constraint — only ACTIVE sensors can log readings
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorOfflineException(sensorId, sensor.getStatus());
        }

        if (reading == null) {
            ErrorResponse err = new ErrorResponse(400, "Bad Request",
                    "Request body is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        if (reading.getMeasurement() == null) {
            ErrorResponse err = new ErrorResponse(400, "Bad Request",
                    "Field 'measurement' is required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getCapturedAt() == null || reading.getCapturedAt() <= 0) {
            reading.setCapturedAt(System.currentTimeMillis());
        }

        InMemoryRegistry.appendReading(sensorId, reading);

        // Keep the parent sensor's latestReading in sync
        sensor.setLatestReading(reading.getMeasurement());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}

