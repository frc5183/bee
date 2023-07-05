package org.team5183.beeapi.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface Entity {
    static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    /**
     * @return Whether the entity is valid
     */
    boolean isValid();
}
