package org.team5183.beeapi.response;

import com.google.gson.JsonElement;

public class BasicResponse {
    public final ResponseStatus status;
    public String message;
    public JsonElement data;

    /**
     * Creates a new BasicResponse with the given status.
     * @param status The status of the response
     */
    public BasicResponse(ResponseStatus status) {
        this.status = status;
    }

    /**
     * Creates a new BasicResponse with the given status and message.
     * @param status The status of the response
     * @param message The message of the response
     */
    public BasicResponse(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Creates a new BasicResponse with the given status and data.
     * @param status The status of the response
     * @param data The data of the response (should probably be an object (and should also probably be an entity))
     */
    public BasicResponse(ResponseStatus status, JsonElement data) {
        this.status = status;
        this.data = data;
    }


    /**
     * Creates a new BasicResponse with the given status, message, and data.
     * @param status The status of the response
     * @param message The message of the response
     * @param data The data of the response (should probably be an object (and should also probably be an entity))
     */
    public BasicResponse(ResponseStatus status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * @return The status of the response
     */
    public ResponseStatus getStatus() {
        return status;
    }

    /**
     * @return The message of the response
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return The data of the response (should probably be an object (and should also probably be an entity))
     */
    public JsonElement getData() {
        return data;
    }
}
