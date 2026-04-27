package uk.ac.campus.api.model;

/**
 * Represents a sensor device installed inside a campus room.
 * Valid status values: ACTIVE | MAINTENANCE | OFFLINE
 */
public class Sensor {

    private String id;             // Unique identifier, e.g. "CO2-042"
    private String type;           // Category: Temperature, CO2, Occupancy, etc.
    private String status;         // Operational state
    private double latestReading;  // Most recent measurement value
    private String roomId;         // ID of the room this sensor belongs to

    public Sensor() {}

    public Sensor(String id, String type, String status, double latestReading, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.latestReading = latestReading;
        this.roomId = roomId;
    }

    // Getters
    public String getId()              { return id; }
    public String getType()            { return type; }
    public String getStatus()          { return status; }
    public double getLatestReading()   { return latestReading; }
    public String getRoomId()          { return roomId; }

    // Setters
    public void setId(String id)                      { this.id = id; }
    public void setType(String type)                  { this.type = type; }
    public void setStatus(String status)              { this.status = status; }
    public void setLatestReading(double latestReading){ this.latestReading = latestReading; }
    public void setRoomId(String roomId)              { this.roomId = roomId; }
}
