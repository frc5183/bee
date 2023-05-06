package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;

public abstract class Endpoint {
    static final Gson gson = new Gson();

    public Endpoint() {
        registerEndpoints();
    }

    abstract void registerEndpoints();
}
