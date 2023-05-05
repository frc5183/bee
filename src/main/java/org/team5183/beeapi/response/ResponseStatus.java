package org.team5183.beeapi.response;

public enum ResponseStatus {
    SUCCESS("success"),
    FAILURE("failure");

    private String status;

    ResponseStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
