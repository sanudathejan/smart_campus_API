package uk.ac.campus.api.resource;

import uk.ac.campus.api.exception.RoomOccupiedException;
import uk.ac.campus.api.model.ErrorResponse;
import uk.ac.campus.api.model.Room;
import uk.ac.campus.api.store.InMemoryRegistry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages the /api/v1/rooms collection.
 *
 * Endpoints:
 *   GET    /api/v1/rooms           — list all rooms
 *   POST   /api/v1/rooms           — register a new room
 *   GET    /api/v1/rooms/{roomId}  — fetch one room by ID
 *   DELETE /api/v1/rooms/{roomId}  — decommission a room (blocked if sensors present)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     *
     * Returns the complete list of registered rooms.
     *
     * Returning full objects vs IDs only (answered in report):
     * Returning only IDs minimises payload size and is efficient when the
     * client only needs a summary, but forces N additional round-trips to
     * fetch each room's details (the N+1 problem). Returning full objects
     * in one response increases bandwidth usage but eliminates those extra
     * calls, improving perceived responsiveness for clients that need full data.
     * The right choice depends on the client's use case; this implementation
     * returns full objects to keep client logic simple.
     */
    @GET
    public Response listRooms() {
        List<Room> all = new ArrayList<>(InMemoryRegistry.rooms().values());
        return Response.ok(all).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Auto-generates an ID if none is supplied.
     * Returns 201 Created with the persisted room in the body.
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null) {
            return badRequest("Request body is required.");
        }
        if (blank(room.getName())) {
            return badRequest("Field 'name' is required.");
        }
        if (room.getCapacity() <= 0) {
            return badRequest("Field 'capacity' must be a positive integer.");
        }

        if (blank(room.getId())) {
            room.setId("ROOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (InMemoryRegistry.roomExists(room.getId())) {
            ErrorResponse err = new ErrorResponse(409, "Conflict",
                    "A room with id '" + room.getId() + "' already exists.");
            return Response.status(Response.Status.CONFLICT).entity(err).build();
        }

        if (room.getSensorIds() == null) room.setSensorIds(new ArrayList<>());

        InMemoryRegistry.saveRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns full details for one room. 404 if not found.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = InMemoryRegistry.findRoom(roomId);
        if (room == null) {
            return notFound("No room found with id '" + roomId + "'.");
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     *
     * Removes a room from the registry.
     * Business constraint: deletion is blocked if the room still has sensors
     * assigned. Attempting to delete such a room throws RoomOccupiedException
     * which is mapped to 409 Conflict by RoomOccupiedMapper.
     *
     * Idempotency (answered in report):
     * A strictly idempotent DELETE would return the same response on every
     * call, even after the resource is gone. This implementation returns 404
     * on subsequent calls after the first successful deletion. The server state
     * does not change after the first call (the room is gone either way), so
     * the operation IS idempotent in terms of side-effects. Returning 404 on
     * repeat calls is a deliberate design choice to give clear feedback that
     * the resource no longer exists, rather than silently pretending it does.
     */
    @DELETE
    @Path("/{roomId}")
    public Response removeRoom(@PathParam("roomId") String roomId) {
        Room room = InMemoryRegistry.findRoom(roomId);

        if (room == null) {
            return notFound("Room '" + roomId + "' does not exist or was already removed.");
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomOccupiedException(roomId);
        }

        InMemoryRegistry.deleteRoom(roomId);
        ErrorResponse confirmation = new ErrorResponse(200, "OK",
                "Room '" + roomId + "' has been successfully decommissioned.");
        return Response.ok(confirmation).build();
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
