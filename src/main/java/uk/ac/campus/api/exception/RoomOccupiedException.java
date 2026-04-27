package uk.ac.campus.api.exception;

/**
 * Raised when a DELETE /rooms/{id} is attempted but the room still
 * has sensors registered inside it. Mapped to HTTP 409 Conflict.
 */
public class RoomOccupiedException extends RuntimeException {

    private final String roomId;

    public RoomOccupiedException(String roomId) {
        super("Room '" + roomId + "' still has sensors attached. "
            + "Remove all sensors before decommissioning the room.");
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
