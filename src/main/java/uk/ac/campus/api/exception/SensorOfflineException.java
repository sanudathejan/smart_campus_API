package uk.ac.campus.api.exception;

/**
 * Raised when a POST /readings is attempted on a sensor that is not ACTIVE.
 * A sensor under MAINTENANCE is physically disconnected and cannot log data.
 * Mapped to HTTP 403 Forbidden.
 */
public class SensorOfflineException extends RuntimeException {

    private final String sensorId;
    private final String currentStatus;

    public SensorOfflineException(String sensorId, String currentStatus) {
        super("Sensor '" + sensorId + "' is currently '" + currentStatus
            + "' and cannot accept new readings at this time.");
        this.sensorId = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId()      { return sensorId; }
    public String getCurrentStatus() { return currentStatus; }
}
