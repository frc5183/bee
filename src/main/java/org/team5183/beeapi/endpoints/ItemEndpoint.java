package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import org.team5183.beeapi.util.Database;
import spark.Request;

import java.sql.SQLException;

import static spark.Spark.*;

public class ItemEndpoint extends Endpoint {
    private static final Logger logger = LogManager.getLogger(ItemEndpoint.class);

    @Override
    void registerEndpoints() {
        path("/items" , () -> {
            get("/all", (req, res) -> {
                try {
                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(Database.getAllItemEntities())));
                } catch (SQLException e) {
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }
            });

            path("/:id", () -> {
                before("", (req, res) -> {
                    if (req.params(":id").equals("all") || req.params(":id").equals("new")) {
                        return;
                    }

                    if (req.params(":id").isEmpty()) {
                        halt(400, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing ID")));
                    }

                    try {
                        Long.parseLong(req.params(":id"));
                    } catch (NumberFormatException e) {
                        halt(400, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "ID must be a number.")));
                    }

                    try {
                        if (Database.getItemEntity(Long.parseLong(req.params(":id"))) == null) {
                            halt(404, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " not found")));
                        }
                    } catch (SQLException e) {
                        halt(500, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error")));
                    }
                });

                get("", (req, res) -> {
                    try {
                        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(Database.getItemEntity(Long.parseLong(req.params(":id"))))));
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }
                });

                delete("", (req, res) -> {
                    try {
                        Database.deleteItemEntity(Database.getItemEntity(Long.parseLong(req.params(":id"))));
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Deleted Item with ID " + req.params(":id")));
                });

                patch("", (req, res) -> {
                    ItemEntity item;
                    try {
                        item = gson.fromJson(req.body(), ItemEntity.class);
                    } catch (JsonSyntaxException e) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                    }

                    try {
                        Database.upsertItemEntity(item);
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated Item with ID " + req.params(":id"), new Gson().toJsonTree(item)));
                });

                path("/checkout", () -> {
                    //todo
                });

            });

            post("/new", (req, res) -> {
                ItemEntity item;
                try {
                    item = gson.fromJson(req.body(), ItemEntity.class);
                } catch (JsonSyntaxException e) {
                    res.status(400);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
                }

                try {
                    Database.upsertItemEntity(item);
                } catch (SQLException e) {
                    logger.debug(e);
                    res.status(500);
                    return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                }

                res.status(201);
                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Created item with ID "+ item.getId(), new Gson().toJsonTree(item)));
            });
        });
    }

}
