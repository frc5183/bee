package org.team5183.beeapi.response;

public enum ResponseStatus {
    SUCCESS("success"),
    ERROR("error");

    private final String status;

    /**
     * Creates a new ResponseStatus with the given status.
     * @param status The status of the response
     */
    ResponseStatus(String status) {
        this.status = status;
    }

    /**
     * @return The status of the response
     */
    public String getStatus() {
        return status;
    }
}
