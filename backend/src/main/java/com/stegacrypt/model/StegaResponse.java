package com.stegacrypt.model;

/**
 * Response model for API responses.
 */
public class StegaResponse {

    private boolean success;
    private String message;
    private Object data;
    private Long timestamp;

    // Constructors
    public StegaResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public StegaResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public StegaResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Static factory methods
    public static StegaResponse ok(String message) { return new StegaResponse(true, message); }
    public static StegaResponse ok(String message, Object data) { return new StegaResponse(true, message, data); }
    public static StegaResponse error(String message) { return new StegaResponse(false, message); }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
