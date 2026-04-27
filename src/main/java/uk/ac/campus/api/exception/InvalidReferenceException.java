package uk.ac.campus.api.exception;

/**
 * Raised when a request payload references an entity that does not exist.
 * Example: POST /sensors with a roomId that has not been created yet.
 * Mapped to HTTP 422 Unprocessable Entity.
 *
 * Why 422 rather than 404?
 * A 404 signals that the requested URL path was not found on the server.
 * Here the URL (/api/v1/sensors) is perfectly valid. The problem is that the
 * JSON body contains a foreign-key reference (roomId) pointing to a resource
 * that does not exist. The request is syntactically correct JSON but
 * semantically unprocessable — which is precisely what 422 is designed for.
 */
public class InvalidReferenceException extends RuntimeException {

    private final String fieldName;
    private final String missingValue;

    public InvalidReferenceException(String fieldName, String missingValue) {
        super("Field '" + fieldName + "' references '" + missingValue
            + "' which does not exist. Create it first.");
        this.fieldName = fieldName;
        this.missingValue = missingValue;
    }

    public String getFieldName()    { return fieldName; }
    public String getMissingValue() { return missingValue; }
}
