package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;

public abstract class Endpoint {
    static final Gson gson = new Gson();

    /**
     * Creates a new endpoint and registers all endpoints for the given endpoint.
     */
    public Endpoint() {
        registerEndpoints();
    }

    /**
     * Registers all endpoints for the given endpoint.
     */
    abstract void registerEndpoints();
}
