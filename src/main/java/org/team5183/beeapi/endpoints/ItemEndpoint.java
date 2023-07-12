package org.team5183.beeapi.endpoints;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.team5183.beeapi.constants.Permission;
import org.team5183.beeapi.entities.CheckoutEntity;
import org.team5183.beeapi.entities.ItemEntity;
import org.team5183.beeapi.middleware.Authentication;
import org.team5183.beeapi.response.BasicResponse;
import org.team5183.beeapi.response.ResponseStatus;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.sql.SQLException;
import java.time.Instant;

import static spark.Spark.*;

public class ItemEndpoint extends Endpoint {
    private static final Logger logger = LogManager.getLogger(ItemEndpoint.class);

    @Override
    public void registerEndpoints() {
        path("/items" , () -> {
            before("", this::authenticate);
            get("all", this::getAllItems);
            post("new", this::newItem);

            path("/:id", () -> {
                before("*", this::isItemExist);

                get("", this::endpointGetItem);
                delete("", this::deleteItem);
                patch("", this::updateItem);

                path("/checkout", () -> {
                    post("", this::checkoutItem);

                    get("/active", this::getItemActiveCheckout);

                    get("/all", this::getAllItemCheckouts);

                    path("/:checkoutId", () -> {
                        get("", this::getItemCheckout);
                        patch("", this::updateItemCheckout);
                        delete("", this::deleteItemCheckout);
                    });
                });

                patch("/return", this::returnItem);
            });
        });
    }

    private Filter isItemExist(Request req, Response res) {
        if (req.params(":id").equals("all") || req.params(":id").equals("new")) return null;

        if (req.params(":id").isEmpty()) end(400, ResponseStatus.ERROR, "Missing ID");

        try {
            Long.parseLong(req.params(":id"));
        } catch (NumberFormatException e) {
            end(400, ResponseStatus.ERROR, "ID must be a number");
        }

        try {
            if (ItemEntity.getItemEntity(Long.parseLong(req.params(":id"))) == null)
                end(404, ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " not found");
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return null;
    }

    private ItemEntity getItem(Request req, Response res) {
        before("", this::isItemExist);

        try {
            return ItemEntity.getItemEntity(Long.parseLong(req.params(":id")));
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return null;
    }

    private String getAllItems(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_ITEMS));

        if (req.queryParams("limit") == null) {
            try {
                return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(ItemEntity.getAllItemEntities())));
            } catch (SQLException e) {
                e.printStackTrace();
                end(500, ResponseStatus.ERROR, "Internal Server Error");
            }
        }

        Long limit = null;
        try {
            limit = req.queryParams("limit") == null ? null : Long.parseLong(req.queryParams("limit"));
        } catch (NumberFormatException e) {
            end(400, ResponseStatus.ERROR, "Limit must be a number.");
        }

        Long offset = null;
        try {
            offset = req.queryParams("offset") == null ? null : Long.parseLong(req.queryParams("offset"));
        } catch (NumberFormatException e) {
            end(400, ResponseStatus.ERROR, "Offset must be a number.");
        }

        try {
            if (limit != null) {
                if (offset != null) {
                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(ItemEntity.getAllItemEntities(limit.intValue(), offset.intValue()))));
                } else {
                    return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(ItemEntity.getAllItemEntities(limit))));
                }
            }
            return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(ItemEntity.getAllItemEntities())));
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return null;
    }

    private String newItem(Request request, Response response) {
        before("", Authentication.checkPermission(request, response, Permission.CAN_CREATE_ITEMS));
        ItemEntity item = this.objectFromBody(request, ItemEntity.class);
        assert item != null;

        if (!item.isValid()) {
            end(400, ResponseStatus.ERROR, "Invalid item data");
        }

        try {
            item.create();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        response.status(201);
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Created item with ID "+ item.getId(), gson.toJsonTree(item)));
    }

    private String endpointGetItem(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_ITEMS));
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(getItem(req, res))));
    }

    private String deleteItem(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_DELETE_ITEMS));
        before("", this::isItemExist);
        try {
            ItemEntity item = ItemEntity.getItemEntity(Long.parseLong(req.params(":id")));
            if (item == null) end(404, ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " not found");
            assert item != null;
            item.delete();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Deleted Item with ID " + req.params(":id")));
    }

    private String updateItem(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_EDIT_ITEMS));
        before("", this::isItemExist);
        ItemEntity item = this.getItem(req, res);
        ItemEntity newItem = this.objectFromBody(req, ItemEntity.class);
        assert item != null;
        assert newItem != null;

        if (newItem.getName() != null) item.setName(newItem.getName());
        if (newItem.getDescription() != null) item.setDescription(newItem.getDescription());
        if (newItem.getPhoto() != null) item.setPhoto(newItem.getPhoto());
        if (newItem.getPrice() != null) item.setPrice(newItem.getPrice());
        if (newItem.getRetailer() != null) item.setRetailer(newItem.getRetailer());
        if (newItem.getPartNumber() != null) item.setPartNumber(newItem.getPartNumber());

        if (!item.isValid()) {
            end(400, ResponseStatus.ERROR, "Invalid item data");
        }

        try {
            item.update();
        } catch (SQLException e) {
            e.printStackTrace();
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated Item with ID " + req.params(":id"), gson.toJsonTree(item)));
    }

    private CheckoutEntity getCheckout(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_CHECKOUTS));
        ItemEntity item = getItem(req, res);
        assert item != null;

        CheckoutEntity checkout = null;
        try {
            checkout = item.getCheckoutEntities().stream().filter(checkoutEntity -> checkoutEntity.getId() == Long.parseLong(req.params(":checkoutId"))).findFirst().orElse(null);
        } catch (NumberFormatException e) {
            end(400, ResponseStatus.ERROR, "Invalid checkout ID");
        }

        if (checkout == null) {
            end(404, ResponseStatus.ERROR, "Checkout with ID " + req.params(":checkoutId") + " not found");
        }

        checkout.setItem(item);

        return checkout;
    }

    private String getItemActiveCheckout(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_CHECKOUTS));
        ItemEntity item = getItem(req, res);
        assert item != null;

        if (item.getCheckoutEntity() == null) {
            end(404, ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " is not checked out");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(item.getCheckoutEntity())));
    }

    private String getAllItemCheckouts(Request req, Response res) {
        before("", this.checkPermission(req, res, Permission.CAN_VIEW_CHECKOUTS));
        ItemEntity item = getItem(req, res);
        assert item != null;

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(item.getCheckoutEntities())));
    }

    private String checkoutItem(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_CHECKOUT_ITEMS));
        ItemEntity item = getItem(req, res);
        assert item != null;

        CheckoutEntity checkout = objectFromBody(req, CheckoutEntity.class);
        assert checkout != null;

        if (item.getCheckoutEntity() != null) end(400, ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " is already checked out");

        checkout.setItem(item);
        checkout.setActive(true);
        checkout.setDate(Instant.now().toEpochMilli());
        item.setCheckoutEntity(checkout);

        if (!checkout.isValid() || !item.isValid()) end(400, ResponseStatus.ERROR, "Invalid checkout data");

        try {
            item.update();
        } catch (SQLException e) {
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Checked out Item with ID " + req.params(":id"), gson.toJsonTree(checkout)));
    }

    private String getItemCheckout(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_VIEW_CHECKOUTS));
        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, gson.toJsonTree(getCheckout(req, res))));
    }

    private String returnItem(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_RETURN_ITEMS));
        ItemEntity item = getItem(req, res);
        assert item != null;

        CheckoutEntity checkout = item.getCheckoutEntity();
        if (checkout == null) {
            end(404, ResponseStatus.ERROR, "Item with ID " + req.params(":id") + " is not checked out");
        }
        assert checkout != null;

        checkout.setActive(false);
        item.setCheckoutEntity(null);

        if (!item.isValid()) end(400, ResponseStatus.ERROR, "Invalid return data");

        try {
            item.update();
        } catch (SQLException e) {
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Returned Item with ID " + req.params(":id"), gson.toJsonTree(checkout)));
    }

    private String updateItemCheckout(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_EDIT_CHECKOUTS));
        CheckoutEntity checkout = getCheckout(req, res);
        assert checkout != null;

        CheckoutEntity newCheckout = objectFromBody(req, CheckoutEntity.class);
        assert newCheckout != null;

        checkout.setBy((newCheckout.getBy() == null || newCheckout.getBy().isEmpty() || newCheckout.getBy().isBlank()) ? checkout.getBy() : newCheckout.getBy());
        checkout.setReason((newCheckout.getReason() == null || newCheckout.getReason().isEmpty() || newCheckout.getReason().isBlank()) ? checkout.getReason() : newCheckout.getReason());
        checkout.setDate(newCheckout.getDate() == null ? checkout.getDate() : newCheckout.getDate());
        checkout.setReturnDate(newCheckout.getReturnDate() == null ? checkout.getReturnDate() : newCheckout.getReturnDate());

        if (newCheckout.isActive() != null) {
            if (newCheckout.isActive() && checkout.getItem().getCheckoutEntity() != null) {
                end(400, ResponseStatus.ERROR, "Item with ID " + checkout.getItem().getId() + " is already checked out");
            } else if (newCheckout.isActive()) {
                checkout.getItem().setCheckoutEntity(checkout);
            } else if (!newCheckout.isActive()) {
                checkout.getItem().setCheckoutEntity(null);
                checkout.setActive(false);
            }
        }

        if (!checkout.isValid() || !checkout.getItem().isValid()) end(400, ResponseStatus.ERROR, "Invalid checkout data");

        try {
            checkout.getItem().update();
        } catch (SQLException e) {
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Updated checkout with ID " + req.params(":checkoutId"), gson.toJsonTree(checkout)));
    }

    private String deleteItemCheckout(Request req, Response res) {
        before("", Authentication.checkPermission(req, res, Permission.CAN_EDIT_CHECKOUTS));
        CheckoutEntity checkout = getCheckout(req, res);

        ItemEntity item = checkout.getItem();
        if (item.getCheckoutEntity() != null) {
            if (checkout.getId().equals(item.getCheckoutEntity().getId())) end(400, ResponseStatus.ERROR, "Cannot delete active checkout");
        }
        item.removeCheckoutEntity(checkout);

        try {
            item.update();
        } catch (SQLException e) {
            end(500, ResponseStatus.ERROR, "Internal Server Error");
        }

        return gson.toJson(new BasicResponse(ResponseStatus.SUCCESS, "Deleted checkout with ID " + req.params(":checkoutId")));
    }
}
