package uk.ac.campus.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * A single historical data point captured by a sensor.
 */
public class Reading {

    private String id;         // Unique event ID (UUID recommended)
    @JsonAlias("timestamp")
    private Long capturedAt;   // Unix epoch milliseconds when reading was taken
    @JsonAlias("value")
    private Double measurement; // The numeric value recorded

    public Reading() {}

    public Reading(String id, Long capturedAt, Double measurement) {
        this.id = id;
        this.capturedAt = capturedAt;
        this.measurement = measurement;
    }

    // Getters
    public String getId()          { return id; }
    public Long getCapturedAt()    { return capturedAt; }
    public Double getMeasurement() { return measurement; }

    // Setters
    public void setId(String id)                    { this.id = id; }
    public void setCapturedAt(Long capturedAt)      { this.capturedAt = capturedAt; }
    public void setMeasurement(Double measurement)  { this.measurement = measurement; }
}
