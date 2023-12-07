package org.team5183.beeapi.entities;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.Nullable;

@DatabaseTable(tableName = "bee_checkouts")
public class CheckoutEntity implements Entity {
    @Expose(serialize = true, deserialize = false)
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private ItemEntity item;

    @Expose
    @DatabaseField(canBeNull = false)
    private String by;

    @Expose
    @DatabaseField(canBeNull = false)
    private String reason;

    @Expose
    @DatabaseField(canBeNull = false)
    private Long date;

    @Expose
    @DatabaseField(canBeNull = true)
    private @Nullable Long returnDate;

    @Expose
    @DatabaseField(canBeNull = false)
    private Boolean active;

    /**
     * @param by The name of the person who checked out the item
     * @param date The date the item was checked in milliseconds since epoch
     */
    public CheckoutEntity(ItemEntity item, String by, String reason, Long date) {
        this.item = item;
        this.by = by;
        this.reason = reason;
        this.date = date;
        this.active = true;
    }

    /**
     * @return The ID of the checkout
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id The ID of the checkout
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return The item that was checked out
     */
    public ItemEntity getItem() {
        return item;
    }

    /**
     * @param item The item that was checked out
     */
    public void setItem(ItemEntity item) {
        this.item = item;
    }

    /**
     * @return The name of the person who checked out the item
     */
    public String getBy() {
        return by;
    }

    /**
     * @param by The name of the person who checked out the item
     */
    public void setBy(String by) {
        this.by = by;
    }

    /**
     * @return The reason the item was checked out
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason The reason the item was checked out
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return The date the item was checked in milliseconds since epoch
     */
    public Long getDate() {
        return date;
    }

    /**
     * @param date The date the item was checked in milliseconds since epoch
     */
    public void setDate(Long date) {
        this.date = date;
    }

    /**
     * @return The date the item was returned in milliseconds since epoch
     */
    public @Nullable Long getReturnDate() {
        return returnDate;
    }

    /**
     * @param returnDate The date the item was returned in milliseconds since epoch
     */
    public void setReturnDate(@Nullable Long returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * @return Whether the item is currently checked out
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * @param active Whether the item is currently checked out
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public boolean isValid() {
        return item != null &&
                by != null && !by.isEmpty() &&
                reason != null && !reason.isEmpty() &&
                date != null && date > 0 &&
                active != null;
    }
}
