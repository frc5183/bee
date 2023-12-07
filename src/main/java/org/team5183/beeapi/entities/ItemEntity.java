package org.team5183.beeapi.entities;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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

    @DatabaseField(canBeNull = true, foreign = true)
    public @Nullable CheckoutEntity checkout;

    @DatabaseField(canBeNull = true, foreign = true)
    public ArrayList<CheckoutEntity> checkouts;

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
    }

    /**
     *  Default constructor for ORMLite.
     */
    public ItemEntity() {
    }

    /**
     * @return The ID of the item
     */
    public Long getId() {
        return id;
    }

    /**
     * @return The name of the item
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The new name of the item
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The description of the item
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The new description of the item
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The URL of the photo of the item
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * @param photo The new URL of the photo of the item
     */
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    /**
     * @return The price of the item
     */
    public Double getPrice() {
        return price;
    }

    /**
     * @param price The new price of the item
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * @return The retailer of the item
     */
    public @Nullable String getRetailer() {
        return retailer;
    }

    /**
     * @param retailer The new retailer of the item
     */
    public void setRetailer(@Nullable String retailer) {
        this.retailer = retailer == null ? "" : retailer;
    }

    /**
     * @return The part number of the item
     */
    public @Nullable String getPartNumber() {
        return partNumber;
    }

    /**
     * @param partNumber The new part number of the item
     */
    public void setPartNumber(@Nullable String partNumber) {
        this.partNumber = partNumber == null ? "" : partNumber;
    }

    /**
     * @return The checkout entity of the item
     */
    public @Nullable CheckoutEntity getCheckout() {
        return checkout;
    }

    /**
     * @param checkout The new checkout entity of the item
     */
    public void setCheckoutEntity(@Nullable CheckoutEntity checkout) {
        this.checkout = checkout;
        addCheckoutEntity(checkout);
    }

    /**
     * @return The checkout entities of the item
     */
    public ArrayList<CheckoutEntity> getCheckouts() {
        return checkouts;
    }

    /**
     * @param checkout The checkout entity to add to the item
     */
    public void addCheckoutEntity(CheckoutEntity checkout) {
        this.checkouts.add(checkout);
    }

    /**
     * @param checkout The checkout entity to remove from the item
     */
    public void removeCheckoutEntity(CheckoutEntity checkout) {
        this.checkouts.remove(checkout);
    }

    /**
     * @param checkoutEntities The new checkout entities of the item
     */
    public void setCheckoutEntities(ArrayList<CheckoutEntity> checkoutEntities) {
        this.checkouts = checkoutEntities;
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
