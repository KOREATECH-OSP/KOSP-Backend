package kr.ac.koreatech.sw.kosp.domain.github.model;

public enum FailureType {
    CONNECTION_CLOSED("Connection prematurely closed"),
    TIMEOUT("Request timeout"),
    RATE_LIMIT("Rate limit exceeded"),
    NOT_FOUND("Resource not found (404)"),
    UNAUTHORIZED("Unauthorized (401)"),
    SERVER_ERROR("Server error (5xx)"),
    NETWORK_ERROR("Network error"),
    UNKNOWN("Unknown error");

    private final String description;

    FailureType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
