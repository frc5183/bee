package org.team5183.beeapi.entities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

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

    private transient @NotNull Collection<CheckoutEntity> checkoutEntities;


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
        this.checkoutEntities = new HashSet<>();
        this.checkouts = new Gson().toJson(checkoutEntities);
    }


    /**
     * This constructor is and should only be used by JPA and ORMLite.
     */
    private ItemEntity() {
        if (checkouts == null || checkouts.isBlank() || checkouts.isEmpty()) {
            checkouts = "";
            this.checkoutEntities = new HashSet<>();
        } else {
            new Gson().fromJson(checkouts, CheckoutEntity[].class);
        }

        new Gson().fromJson(checkout, CheckoutEntity.class);
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
        this.retailer = retailer;
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
        this.partNumber = partNumber;
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
            for (CheckoutEntity checkout : checkoutEntities) {
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
    public synchronized @NotNull Collection<CheckoutEntity> getCheckoutEntities() {
        return checkoutEntities;
    }

    /**
     * @param checkout The checkout entity to add to the item
     */
    public synchronized void addCheckoutEntity(@NotNull CheckoutEntity checkout) {
        this.checkoutEntities.add(checkout);
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    /**
     * @param checkout The checkout entity to remove from the item
     */
    public synchronized void removeCheckoutEntity(@NotNull CheckoutEntity checkout) {
        this.checkoutEntities.remove(checkout);
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    /**
     * @param checkoutEntities The new checkout entities of the item
     */
    public synchronized void setCheckoutEntities(@NotNull Collection<CheckoutEntity> checkoutEntities) {
        this.checkouts = new Gson().toJson(checkoutEntities);
        this.checkoutEntities = checkoutEntities;
    }
}
