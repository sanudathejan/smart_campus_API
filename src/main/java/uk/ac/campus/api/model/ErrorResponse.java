package uk.ac.campus.api.model;

/**
 * Uniform JSON structure returned for every API error.
 * Using a consistent error envelope means API consumers can always
 * expect the same fields regardless of which endpoint failed.
 */
public class ErrorResponse {

    private int httpStatus;
    private String errorType;
    private String detail;
    private long occurredAt;

    public ErrorResponse() {}

    public ErrorResponse(int httpStatus, String errorType, String detail) {
        this.httpStatus = httpStatus;
        this.errorType = errorType;
        this.detail = detail;
        this.occurredAt = System.currentTimeMillis();
    }

    // Getters
    public int getHttpStatus()    { return httpStatus; }
    public String getErrorType()  { return errorType; }
    public String getDetail()     { return detail; }
    public long getOccurredAt()   { return occurredAt; }

    // Setters
    public void setHttpStatus(int httpStatus)      { this.httpStatus = httpStatus; }
    public void setErrorType(String errorType)     { this.errorType = errorType; }
    public void setDetail(String detail)           { this.detail = detail; }
    public void setOccurredAt(long occurredAt)     { this.occurredAt = occurredAt; }
}
