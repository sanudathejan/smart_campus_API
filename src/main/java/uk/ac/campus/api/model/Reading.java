package uk.ac.campus.api.model;

/**
 * A single historical data point captured by a sensor.
 */
public class Reading {

    private String id;         // Unique event ID (UUID recommended)
    private long capturedAt;   // Unix epoch milliseconds when reading was taken
    private double measurement; // The numeric value recorded

    public Reading() {}

    public Reading(String id, long capturedAt, double measurement) {
        this.id = id;
        this.capturedAt = capturedAt;
        this.measurement = measurement;
    }

    // Getters
    public String getId()          { return id; }
    public long getCapturedAt()    { return capturedAt; }
    public double getMeasurement() { return measurement; }

    // Setters
    public void setId(String id)                    { this.id = id; }
    public void setCapturedAt(long capturedAt)      { this.capturedAt = capturedAt; }
    public void setMeasurement(double measurement)  { this.measurement = measurement; }
}
