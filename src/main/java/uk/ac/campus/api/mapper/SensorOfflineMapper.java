package uk.ac.campus.api.mapper;

import uk.ac.campus.api.exception.SensorOfflineException;
import uk.ac.campus.api.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts SensorOfflineException into HTTP 403 Forbidden.
 *
 * 403 is appropriate here because the server fully understands the request
 * but is refusing to process it due to the sensor's current operational state.
 * The client is not authorised to post data to a sensor that is offline.
 */
@Provider
public class SensorOfflineMapper implements ExceptionMapper<SensorOfflineException> {

    @Override
    public Response toResponse(SensorOfflineException ex) {
        ErrorResponse body = new ErrorResponse(
                403, "Forbidden", ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
