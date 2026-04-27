package uk.ac.campus.api.mapper;

import uk.ac.campus.api.exception.InvalidReferenceException;
import uk.ac.campus.api.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts InvalidReferenceException into HTTP 422 Unprocessable Entity.
 *
 * 422 is more semantically accurate than 404 here because:
 * - 404 means the target URL was not found on the server
 * - The target URL (/api/v1/sensors) is completely valid and reachable
 * - The issue is that the JSON payload references a resource (roomId) that
 *   does not exist — making the request semantically unprocessable
 * - 422 was designed precisely for this scenario: valid syntax, invalid semantics
 */
@Provider
public class InvalidReferenceMapper implements ExceptionMapper<InvalidReferenceException> {

    @Override
    public Response toResponse(InvalidReferenceException ex) {
        ErrorResponse body = new ErrorResponse(
                422, "Unprocessable Entity", ex.getMessage());
        return Response.status(422)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
