package org.team5183.beeapi.entities;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;

public class CheckoutEntity implements Entity {
    @Expose
    private Long id;

    private ItemEntity item;

    @Expose
    private String by;

    @Expose
    private String reason;

    @Expose
    private Long date;

    @Expose
    private @Nullable Long returnDate;

    @Expose
    private Boolean active;

    /**
     * @param by The name of the person who checked out the item
     * @param date The date the item was checked in milliseconds since epoch
     */
    public CheckoutEntity(ItemEntity item, String by, String reason, Long date) {
        this.id = item.getCheckoutEntities().size() == 0 ? 1L : (item.getCheckoutEntities().get(item.getCheckoutEntities().size() - 1).getId() + 1L);
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
    public synchronized String getBy() {
        return by;
    }

    /**
     * @param by The name of the person who checked out the item
     */
    public synchronized void setBy(String by) {
        this.by = by;
    }

    /**
     * @return The reason the item was checked out
     */
    public synchronized String getReason() {
        return reason;
    }

    /**
     * @param reason The reason the item was checked out
     */
    public synchronized void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return The date the item was checked in milliseconds since epoch
     */
    public synchronized Long getDate() {
        return date;
    }

    /**
     * @param date The date the item was checked in milliseconds since epoch
     */
    public synchronized void setDate(Long date) {
        this.date = date;
    }

    /**
     * @return The date the item was returned in milliseconds since epoch
     */
    public synchronized @Nullable Long getReturnDate() {
        return returnDate;
    }

    /**
     * @param returnDate The date the item was returned in milliseconds since epoch
     */
    public synchronized void setReturnDate(@Nullable Long returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * @return Whether the item is currently checked out
     */
    public synchronized Boolean isActive() {
        return active;
    }

    /**
     * @param active Whether the item is currently checked out
     */
    public synchronized void setActive(Boolean active) {
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
