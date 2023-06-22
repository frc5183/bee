package org.team5183.beeapi.entities;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.team5183.beeapi.runnables.DatabaseRequestRunnable;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@DatabaseTable(tableName = "bee_items")
public class ItemEntity {
    @Expose(serialize = true, deserialize = false)
    @DatabaseField(generatedId = true)
    private @NotNull Long id;

    @Expose
    @DatabaseField(canBeNull = false)
    private @NotNull String name;

    @Expose
    @DatabaseField(canBeNull = false)
    private @NotNull String description;

    @Expose
    @Column(nullable = false)
    private @NotNull String photo;

    @Expose
    @DatabaseField(canBeNull = false)
    private @NotNull Double price;

    @Expose
    @DatabaseField(canBeNull = true)
    private @NotNull String retailer;

    @Expose
    @DatabaseField(canBeNull = true)
    private @NotNull String partNumber;

    @DatabaseField(canBeNull = true)
    public @Nullable String checkout;

    @DatabaseField(canBeNull = false)
    public @NotNull String checkouts;

    private transient @Nullable CheckoutEntity checkoutEntity;

    private transient @NotNull HashMap<Long, CheckoutEntity> checkoutEntities;


    /**
     * @param name        The name of the item
     * @param description The description of the item
     * @param photo       The URL of the photo of the item
     * @param price       The price of the item
     * @param retailer    The retailer of the item
     * @param partNumber  The part number of the item
     */
    public ItemEntity(@NotNull String name, @NotNull String description, @NotNull String photo, @NotNull Double price, @Nullable String retailer, @Nullable String partNumber) {
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.price = price;
        this.retailer = retailer == null ? "" : retailer;
        this.partNumber = partNumber == null ? "" : partNumber;
        this.checkoutEntities = new HashMap<>();
        this.checkouts = new Gson().toJson(checkoutEntities);
    }


    /**
     * This constructor is and should only be used by JPA and ORMLite.
     */
    private ItemEntity() {
        if (checkouts == null || checkouts.isBlank() || checkouts.isEmpty()) {
            checkouts = "";
            this.checkoutEntities = new HashMap<>();
        } else {
            checkoutEntities = new Gson().fromJson(checkouts, HashMap.class);
        }

        checkoutEntity = new Gson().fromJson(checkout, CheckoutEntity.class);
    }


    /**
     * Gets the user with the specified ID.
     * @param id The ID of the user.
     * @return The user with the specified ID, or null if no user exists with that ID.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static ItemEntity getItemEntity(long id) throws SQLException {
        CompletableFuture<ItemEntity> future = DatabaseRequestRunnable.itemQuery(DatabaseRequestRunnable.getItemDao().queryBuilder().where().eq("id", id).prepare());
        AtomicReference<ItemEntity> itemEntity = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            itemEntity.set(ie);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return itemEntity.get();
    }

    public static List<ItemEntity> getAllItemEntities() throws SQLException {
        CompletableFuture<List<ItemEntity>> future = DatabaseRequestRunnable.itemQueryMultiple(DatabaseRequestRunnable.getItemDao().queryBuilder().prepare());
        AtomicReference<List<ItemEntity>> itemEntities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            itemEntities.set(ie);
        });

        if (throwable.get() != null) {
            throw new SQLException(throwable.get());
        }

        return itemEntities.get();
    }

    /**
     * Creates the user in the database.
     * TODO: make this not like this, allow it to actually run through the database request runnable's cache instead of just forcing synchronization, there isn't a PreparedCreate for this tho so time to innovate!!!
     * @throws SQLException If an error occurs while creating the user in the database.
     */
    public synchronized void create() throws SQLException {
        DatabaseRequestRunnable.getItemDao().createOrUpdate(this);
    }

    /**
     * Updates the user in the database.
     * @throws SQLException If an error occurs while updating the user in the database.
     */
    public void update() throws SQLException {
        DatabaseRequestRunnable.itemStatement(DatabaseRequestRunnable.getItemDao().updateBuilder().where().eq("id", this.id).prepare());
    }

    /**
     * Deletes the user from the database.
     * @throws SQLException If an error occurs while deleting the user from the database.
     */
    public void delete() throws SQLException {
        DatabaseRequestRunnable.itemStatement(DatabaseRequestRunnable.getItemDao().deleteBuilder().where().eq("id", this.id).prepare());
    }


    /**
     * @return The ID of the item
     */
    public synchronized @NotNull Long getId() {
        return id;
    }

    /**
     * @return The name of the item
     */
    public synchronized @NotNull String getName() {
        return name;
    }

    /**
     * @param name The new name of the item
     */
    public synchronized void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * @return The description of the item
     */
    public synchronized @NotNull String getDescription() {
        return description;
    }

    /**
     * @param description The new description of the item
     */
    public synchronized void setDescription(@NotNull String description) {
        this.description = description;
    }

    /**
     * @return The URL of the photo of the item
     */
    public synchronized @NotNull String getPhoto() {
        return photo;
    }

    /**
     * @param photo The new URL of the photo of the item
     */
    public synchronized void setPhoto(@NotNull String photo) {
        this.photo = photo;
    }

    /**
     * @return The price of the item
     */
    public synchronized @NotNull Double getPrice() {
        return price;
    }

    /**
     * @param price The new price of the item
     */
    public synchronized void setPrice(@NotNull Double price) {
        this.price = price;
    }

    /**
     * @return The retailer of the item
     */
    public synchronized @Nullable String getRetailer() {
        return retailer;
    }

    /**
     * @param retailer The new retailer of the item
     */
    public synchronized void setRetailer(@Nullable String retailer) {
        this.retailer = retailer == null ? "" : retailer;
    }

    /**
     * @return The part number of the item
     */
    public synchronized @Nullable String getPartNumber() {
        return partNumber;
    }

    /**
     * @param partNumber The new part number of the item
     */
    public synchronized void setPartNumber(@Nullable String partNumber) {
        this.partNumber = partNumber == null ? "" : partNumber;
    }

    /**
     * @return The checkout of the item
     */
    public synchronized @Nullable String getCheckout() {
        return checkout;
    }

    /**
     * @param checkout The new checkout of the item
     */
    public synchronized void setCheckout(@Nullable String checkout) {
        this.checkout = checkout;
    }

    /**
     * @return The checkouts of the item
     */
    public synchronized @NotNull String getCheckouts() {
        return checkouts;
    }

    /**
     * @param checkouts The new checkouts of the item
     */
    public synchronized void setCheckouts(@NotNull String checkouts) {
        this.checkouts = checkouts;
    }

    /**
     * @return The checkout entity of the item
     */
    public synchronized @Nullable CheckoutEntity getCheckoutEntity() {
        return checkoutEntity;
    }

    /**
     * @param checkoutEntity The new checkout entity of the item
     */
    public synchronized void setCheckoutEntity(@Nullable CheckoutEntity checkoutEntity) {
        this.checkouts = new Gson().toJson(checkoutEntity);
        this.checkoutEntity = checkoutEntity;

        if (checkoutEntity != null) {
            addCheckoutEntity(checkoutEntity);
        } else {
            for (CheckoutEntity checkout : checkoutEntities.values()) {
                if (checkout.isActive()) {
                    checkout.setActive(false);
                    break;
                }
            }
        }
    }

    /**
     * @return The checkout entities of the item
     */
    public synchronized @NotNull HashMap<Long, CheckoutEntity> getCheckoutEntities() {
        return checkoutEntities;
    }

    /**
     * @param checkout The checkout entity to add to the item
     */
    public synchronized void addCheckoutEntity(@NotNull CheckoutEntity checkout) {
        this.checkoutEntities.put(checkout.getId(), checkout);
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    /**
     * @param checkout The checkout entity to remove from the item
     */
    public synchronized void removeCheckoutEntity(@NotNull CheckoutEntity checkout) {
        this.checkoutEntities.remove(checkout.getId());
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    /**
     * @param checkoutEntities The new checkout entities of the item
     */
    public synchronized void setCheckoutEntities(@NotNull HashMap<Long, CheckoutEntity> checkoutEntities) {
        this.checkouts = new Gson().toJson(checkoutEntities);
        this.checkoutEntities = checkoutEntities;
    }
}
