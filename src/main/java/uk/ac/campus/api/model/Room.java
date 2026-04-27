package uk.ac.campus.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room on the university campus.
 */
public class Room {

    private String id;           // Unique identifier, e.g. "LAB-101"
    private String name;         // Human-readable label, e.g. "Computer Lab A"
    private int capacity;        // Maximum permitted occupancy
    private List<String> sensorIds = new ArrayList<>();  // IDs of sensors installed here

    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    // Getters
    public String getId()               { return id; }
    public String getName()             { return name; }
    public int getCapacity()            { return capacity; }
    public List<String> getSensorIds()  { return sensorIds; }

    // Setters
    public void setId(String id)                    { this.id = id; }
    public void setName(String name)                { this.name = name; }
    public void setCapacity(int capacity)           { this.capacity = capacity; }
    public void setSensorIds(List<String> ids)      { this.sensorIds = ids; }

    // Convenience mutators
    public void attachSensor(String sensorId) {
        if (!sensorIds.contains(sensorId)) sensorIds.add(sensorId);
    }

    public void detachSensor(String sensorId) {
        sensorIds.remove(sensorId);
    }
}
