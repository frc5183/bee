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
                get("", (req, res) -> {
                    if (!isValidId(req)) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid ID"));
                    }

                    try {
                        if (!isExistingItem(req)) {
                            res.status(404);
                            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " does not exist"));
                        }
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    try {
                        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(Database.getItemEntity(Long.parseLong(req.params(":id"))))));
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }
                });
                delete("", (req, res) -> {
                    if (!isValidId(req)) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid ID"));
                    }

                    try {
                        if (!isExistingItem(req)) {
                            res.status(404);
                            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " does not exist"));
                        }
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    try {
                        Database.deleteItemEntity(Database.getItemEntity(Long.parseLong(req.params(":id"))));
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Deleted Item with ID " + req.params(":id")));
                });
                patch("", (req, res) -> {
                    if (!isValidId(req)) {
                        res.status(400);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid ID"));
                    }

                    try {
                        if (!isExistingItem(req)) {
                            res.status(404);
                            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " does not exist"));
                        }
                    } catch (SQLException e) {
                        res.status(500);
                        return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
                    }

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

    private boolean isValidId(Request req) {
        if (req.params(":id") == null) {
            return false;
        }

        try {
            Long.parseLong(req.params(":id"));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isExistingItem(Request req) throws SQLException {
        if (!isValidId(req)) return false;
        return Database.getItemEntity(Long.parseLong(req.params(":id"))) != null;
    }
}
