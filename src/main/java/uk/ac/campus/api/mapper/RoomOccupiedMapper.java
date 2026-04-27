package uk.ac.campus.api.mapper;

import uk.ac.campus.api.exception.RoomOccupiedException;
import uk.ac.campus.api.model.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts RoomOccupiedException into an HTTP 409 Conflict response.
 * @Provider tells Jersey to auto-register this mapper via package scanning.
 */
@Provider
public class RoomOccupiedMapper implements ExceptionMapper<RoomOccupiedException> {

    @Override
    public Response toResponse(RoomOccupiedException ex) {
        ErrorResponse body = new ErrorResponse(
                409, "Room Conflict", ex.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
