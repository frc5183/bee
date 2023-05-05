package org.team5183.beeapi.endpoints;

import static spark.Spark.*;

public class ItemEndpoint implements Endpoint {
    @Override
    public void registerEndpoints() {
        path("/items" , () -> {
            get("/:id" , (req , res) -> {

            });
        });
    }
}
