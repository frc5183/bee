package org.team5183.beeapi.entities;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.Nullable;
import org.team5183.beeapi.runnables.DatabaseRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@DatabaseTable(tableName = "bee_items")
public class ItemEntity implements Entity {
    @Expose(serialize = true, deserialize = false)
    @DatabaseField(generatedId = true)
    private Long id;

    @Expose
    @DatabaseField(canBeNull = false)
    private String name;

    @Expose
    @DatabaseField(canBeNull = false)
    private String description;

    @Expose
    @DatabaseField(canBeNull = false)
    private String photo;

    @Expose
    @DatabaseField(canBeNull = false)
    private Double price;

    @Expose
    @DatabaseField(canBeNull = true)
    private String retailer;

    @Expose
    @DatabaseField(canBeNull = true)
    private String partNumber;

    @DatabaseField(canBeNull = true)
    public @Nullable String checkout;

    @DatabaseField(canBeNull = false)
    public String checkouts;

    private transient @Nullable CheckoutEntity checkoutEntity;

    private transient HashMap<Long, CheckoutEntity> checkoutEntities;


    /**
     * @param name        The name of the item
     * @param description The description of the item
     * @param photo       The URL of the photo of the item
     * @param price       The price of the item
     * @param retailer    The retailer of the item
     * @param partNumber  The part number of the item
     */
    public ItemEntity(String name, String description, String photo, Double price, String retailer, String partNumber) {
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
     *  Default constructor for ORMLite.
     */
    public ItemEntity() {
        this.checkouts = "";
    }


    /**
     * Gets the item with the specified ID.
     * @param id The ID of the item.
     * @return The item with the specified ID, or null if no item exists with that ID.
     * @throws SQLException If an error occurs while querying the database.
     */
    @Nullable
    public static ItemEntity getItemEntity(long id) throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getItemDao().queryBuilder().where().eq("id", id).prepare()));
        AtomicReference<Optional<List<ItemEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        if (future.isCancelled()) return null;
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();

        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() > 1 || entities.get().get().size() < 1) return null;
        return entities.get().get().get(0);
    }

    /**
     * Gets a list of all items in the database.
     * @return A list of all items in the database.
     * @throws SQLException If an error occurs while querying the database.
     * @see #getAllItemEntities(long)
     * @see #getAllItemEntities(int, int)
     */
    public static List<ItemEntity> getAllItemEntities() throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getItemDao().queryBuilder().prepare()));
        AtomicReference<Optional<List<ItemEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();

        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() < 1) return null;
        return entities.get().get();
    }

    /**
     * Gets a list of all items in the database.
     * @param limit The maximum number of items to return.
     * @return A list of all users in the database.
     * @throws SQLException If an error occurs while querying the database.
     * @see #getAllItemEntities()
     * @see #getAllItemEntities(int, int)
     */
    public static List<ItemEntity> getAllItemEntities(long limit) throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getItemDao().queryBuilder().limit(limit).prepare()));
        AtomicReference<Optional<List<ItemEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();

        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() < 1) return null;
        return entities.get().get();
    }

    /**
     * @param limit The maximum number of users to return.
     * @param offset The offset to start at.
     * @return A list of all users in the database.
     * @throws SQLException If an error occurs while querying the database.
     * @see #getAllItemEntities()
     * @see #getAllItemEntities(long)
     */
    public static List<ItemEntity> getAllItemEntities(int limit, int offset) throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getItemDao().queryBuilder().prepare()));
        AtomicReference<Optional<List<ItemEntity>>> entities = new AtomicReference<>();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        future.join();
        future.whenComplete((ie, t) -> {
            throwable.set(t);
            entities.set(ie);
        });
        if (throwable.get() != null) throw (SQLException) throwable.get();

        if (entities.get().isEmpty()) return null;
        if (entities.get().get().size() < 1) return null;

        List<ItemEntity> itemEntities = entities.get().get().subList(offset, entities.get().get().size());
        if (itemEntities.size() > limit) itemEntities = itemEntities.subList(0, limit);
        return itemEntities;
    }

    /**
     * Creates the item in the database.
     * @throws SQLException If an error occurs while creating the user in the database.
     */
    public synchronized void create() throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(this, DatabaseRunnable.DatabaseRequest.RequestType.INSERT));
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        future.join();
        future.whenComplete((ie, t) -> throwable.set(t));

        if (throwable.get() != null) throw (SQLException) throwable.get();
    }

    /**
     * Updates the item in the database.
     * @throws SQLException If an error occurs while updating the user in the database.
     */
    public void update() throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(this, DatabaseRunnable.DatabaseRequest.RequestType.UPDATE));
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        future.join();
        future.whenComplete((ie, t) -> throwable.set(t));

        if (throwable.get() != null) throw (SQLException) throwable.get();
    }

    /**
     * Deletes the item from the database.
     * @throws SQLException If an error occurs while deleting the user from the database.
     */
    public void delete() throws SQLException {
        CompletableFuture<Optional<List<ItemEntity>>> future = DatabaseRunnable.itemRequest(new DatabaseRunnable.DatabaseRequest<>(DatabaseRunnable.getItemDao().deleteBuilder().where().eq("id", this.id).prepare()));
        AtomicReference<Throwable> throwable = new AtomicReference<>();

        future.join();
        future.whenComplete((ie, t) -> throwable.set(t));

        if (throwable.get() != null) throw (SQLException) throwable.get();
    }

    /**
     * @return The ID of the item
     */
    public synchronized Long getId() {
        return id;
    }

    /**
     * @return The name of the item
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * @param name The new name of the item
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * @return The description of the item
     */
    public synchronized String getDescription() {
        return description;
    }

    /**
     * @param description The new description of the item
     */
    public synchronized void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The URL of the photo of the item
     */
    public synchronized String getPhoto() {
        return photo;
    }

    /**
     * @param photo The new URL of the photo of the item
     */
    public synchronized void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * @return The price of the item
     */
    public synchronized Double getPrice() {
        return price;
    }

    /**
     * @param price The new price of the item
     */
    public synchronized void setPrice(Double price) {
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
    public synchronized String getCheckouts() {
        return checkouts;
    }

    /**
     * @param checkouts The new checkouts of the item
     */
    public synchronized void setCheckouts(String checkouts) {
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
    public synchronized HashMap<Long, CheckoutEntity> getCheckoutEntities() {
        if (checkoutEntities == null) checkoutEntities = new HashMap<>();
        return checkoutEntities;
    }

    /**
     * @param checkout The checkout entity to add to the item
     */
    public synchronized void addCheckoutEntity(CheckoutEntity checkout) {
        this.checkoutEntities.put(checkout.getId(), checkout);
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    /**
     * @param checkout The checkout entity to remove from the item
     */
    public synchronized void removeCheckoutEntity(CheckoutEntity checkout) {
        this.checkoutEntities.remove(checkout.getId());
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    /**
     * @param checkoutEntities The new checkout entities of the item
     */
    public synchronized void setCheckoutEntities(HashMap<Long, CheckoutEntity> checkoutEntities) {
        this.checkouts = new Gson().toJson(checkoutEntities);
        this.checkoutEntities = checkoutEntities;
    }

    /**
     * Checks if the ItemEntity is valid.
     * @return Whether the ItemEntity is valid
     */
    public boolean isValid() {
        return this.name != null && !this.name.isEmpty() &&
                this.description != null &&
                this.photo != null &&
                this.price != null;
    }
}
