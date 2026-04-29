package com.smartcampus.resource;

import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Part 4 - Sub-resource for sensor readings.
 * Handles GET and POST for /api/v1/sensors/{sensorId}/readings
 * Instantiated by SensorResource via the sub-resource locator pattern.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store;

    public SensorReadingResource(String sensorId, DataStore store) {
        this.sensorId = sensorId;
        this.store = store;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadingsForSensor(sensorId);
        return Response.ok(history).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = store.getSensors().get(sensorId);

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under MAINTENANCE "
                    + "and cannot accept new readings."
            );
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(400, "Bad Request",
                            "Request body is required with a numeric 'value' field."))
                    .build();
        }

        SensorReading newReading = new SensorReading(reading.getValue());
        store.addReading(sensorId, newReading);
        sensor.setCurrentValue(newReading.getValue());

        return Response.created(uriInfo.getAbsolutePathBuilder().path(newReading.getId()).build())
                .entity(newReading)
                .build();
    }
}
