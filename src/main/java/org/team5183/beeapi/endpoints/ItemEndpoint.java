package org.team5183.beeapi.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.entities.CheckoutEntity;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.middleware.Authentication;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import org.team5183.beeapi.util.Database;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.time.Instant;

import static spark.Spark.*;

public class ItemEndpoint extends Endpoint {
    private static final Logger logger = LogManager.getLogger(ItemEndpoint.class);

    @Override
    void registerEndpoints() {
        path("/items" , () -> {
            before("*", this::authenticate);
            get("/all", this::getAllItems);

            path("/:id", () -> {
                before("", this::isItemExist);

                get("", this::getItem);

                delete("", this::deleteItem);

                patch("", this::updateItem);

                path("/checkout", () -> {
                    get("/active", this::getItemActiveCheckout);

                    get("/all", this::getAllItemCheckouts);
                });

                patch("/checkout", this::checkoutItem);
            });

            post("/return", this::returnItem);

            post("/new", this::newItem);
        });
    }

    private Filter isItemExist(Request req, Response res) {
        if (req.params(":id").equals("all") || req.params(":id").equals("new")) return null;

        if (req.params(":id").isEmpty())
            halt(400, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Missing ID")));

        try {
            Long.parseLong(req.params(":id"));
        } catch (NumberFormatException e) {
            halt(400, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "ID must be a number.")));
        }

        try {
            if (Database.getItemEntity(Long.parseLong(req.params(":id"))) == null)
                halt(404, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " not found")));
        } catch (SQLException e) {
            logger.error(e);
            halt(500, gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error")));
        }

        return null;
    }

    private String getAllItems(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_ITEMS));
        try {
            return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(Database.getAllItemEntities())));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }
    }

    private String getItem(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_ITEMS));
        try {
            return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(Database.getItemEntity(Long.parseLong(req.params(":id"))))));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }
    }

    private String deleteItem(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_DELETE_ITEMS));
        try {
            Database.deleteItemEntity(Database.getItemEntity(Long.parseLong(req.params(":id"))));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Deleted Item with ID " + req.params(":id")));
    }

    private String updateItem(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_EDIT_ITEMS));
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

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated Item with ID " + req.params(":id"), item.toJson()));
    }

    private String getItemActiveCheckout(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_CHECKOUTS));
        ItemEntity item;
        try {
            item = Database.getItemEntity(Long.parseLong(req.params(":id")));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }
        assert item != null;

        if (item.getCheckoutEntity() == null) {
            res.status(404);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " is not checked out"));
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(item.getCheckoutEntity())));
    }

    private String getAllItemCheckouts(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_CHECKOUTS));
        ItemEntity item;
        try {
            item = Database.getItemEntity(Long.parseLong(req.params(":id")));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }
        assert item != null;
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(item.getCheckoutEntities())));
    }

    private String checkoutItem(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_CHECKOUT_ITEMS));
        ItemEntity item;
        try {
            item = Database.getItemEntity(Long.parseLong(req.params(":id")));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }
        assert item != null;

        CheckoutEntity checkout;
        try {
            checkout = gson.fromJson(req.body(), CheckoutEntity.class);
        } catch (JsonSyntaxException e) {
            res.status(400);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
        }

        if (checkout == null) {
            res.status(400);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
        }

        checkout.setActive(true);

        item.setCheckoutEntity(checkout);
        try {
            Database.upsertItemEntity(item);
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Checked out Item with ID " + req.params(":id")));
    }

    private String returnItem(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_RETURN_ITEMS));
        ItemEntity item;
        try {
            item = Database.getItemEntity(Long.parseLong(req.params(":id")));
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }

        assert item != null;

        CheckoutEntity checkout = item.getCheckoutEntity();
        if (checkout == null) {
            res.status(404);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " is not checked out"));
        }

        item.setCheckoutEntity(null);
        item.removeCheckoutEntity(checkout);
        checkout.setActive(false);
        checkout.setReturnDate(Instant.now().toEpochMilli());
        item.addCheckoutEntity(checkout);

        try {
            Database.upsertItemEntity(item);
        } catch (SQLException e) {
            logger.error(e);
            res.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Returned Item with ID " + req.params(":id")));
    }

    private String newItem(Request request, Response response) {
        before("", Authentication.checkPermission(request, response, Permission.CAN_CREATE_ITEMS));
        ItemEntity item;

        try {
            item = gson.fromJson(request.body(), ItemEntity.class);
        } catch (JsonSyntaxException e) {
            response.status(400);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Invalid Body"));
        }

        try {
            Database.upsertItemEntity(item);
        } catch (SQLException e) {
            logger.debug(e);
            logger.error(e);
            response.status(500);
            return gson.toJson(new BasicResponse(ResponseStatus.ERROR, "Internal Server Error"));
        }
        response.status(201);
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Created item with ID "+ item.getId(), new Gson().toJsonTree(item)));
    }
}
