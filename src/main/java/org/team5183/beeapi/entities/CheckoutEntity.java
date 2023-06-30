package org.team5183.beeapi.entities;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckoutEntity {
    @Expose(serialize = true, deserialize = false)
    private @NotNull Long id;

    private @NotNull ItemEntity item;

    @Expose
    private @NotNull String by;

    @Expose
    private @NotNull String reason;

    @Expose
    private @NotNull Long date;

    @Expose
    private @Nullable Long returnDate;

    @Expose
    private @NotNull Boolean active;

    /**
     * @param by The name of the person who checked out the item
     * @param date The date the item was checked in milliseconds since epoch
     */
    public CheckoutEntity(@NotNull ItemEntity item, @NotNull String by, @Nullable String reason, @NotNull Long date) {
        this.id = (item.getCheckoutEntities().size() + 1L);
        this.item = item;
        this.by = by;
        this.reason = reason == null ? "" : reason;
        this.date = date;
        this.active = true;
    }

    /**
     * @return The name of the person who checked out the item
     */
    public synchronized @NotNull String getBy() {
        return by;
    }

    /**
     * @return The ID of the checkout
     */
    public @NotNull Long getId() {
        return id;
    }

    /**
     * @param id The ID of the checkout
     */
    public void setId(@NotNull Long id) {
        this.id = id;
    }

    /**
     * @return The item that was checked out
     */
    public @NotNull ItemEntity getItem() {
        return item;
    }

    /**
     * @param item The item that was checked out
     */
    public void setItem(@NotNull ItemEntity item) {
        this.item = item;
    }

    /**
     * @param by The name of the person who checked out the item
     */
    public synchronized void setBy(@NotNull String by) {
        this.by = by;
    }

    /**
     * @return The reason the item was checked out
     */
    public synchronized @NotNull String getReason() {
        return reason;
    }

    /**
     * @param reason The reason the item was checked out
     */
    public synchronized void setReason(@NotNull String reason) {
        this.reason = reason;
    }

    /**
     * @return The date the item was checked in milliseconds since epoch
     */
    public synchronized @NotNull Long getDate() {
        return date;
    }

    /**
     * @param date The date the item was checked in milliseconds since epoch
     */
    public synchronized void setDate(@NotNull Long date) {
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
    public synchronized @NotNull Boolean isActive() {
        return active;
    }

    /**
     * @param active Whether the item is currently checked out
     */
    public synchronized void setActive(@NotNull Boolean active) {
        this.active = active;
    }
}
