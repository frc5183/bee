package org.team5183.beeapi.response;

import com.google.gson.JsonElement;

public class BasicResponse {
    public ResponseStatus status;
    public String message;
    public JsonElement data;

    public BasicResponse(ResponseStatus status) {
        this.status = status;
    }

    public BasicResponse(ResponseStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public BasicResponse(ResponseStatus status, JsonElement data) {
        this.status = status;
        this.data = data;
    }

    public BasicResponse(ResponseStatus status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public JsonElement getData() {
        return data;
    }
}
