package org.team5183.beeapi.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CheckoutEntity {
    private @NotNull String checkoutName;

    private @NotNull Long checkoutDate;

    private @Nullable Long returnDate;

    private @NotNull Boolean active;

    /**
     * @param checkoutName The name of the person who checked out the item
     * @param checkoutDate The date the item was checked in milliseconds since epoch
     */
    public CheckoutEntity(@NotNull String checkoutName, @NotNull Long checkoutDate) {
        this.checkoutName = checkoutName;
        this.checkoutDate = checkoutDate;
        this.active = true;
    }

    /**
     * @return The name of the person who checked out the item
     */
    public @NotNull String getCheckoutName() {
        return checkoutName;
    }

    /**
     * @param checkoutName The name of the person who checked out the item
     */
    public void setCheckoutName(@NotNull String checkoutName) {
        this.checkoutName = checkoutName;
    }

    /**
     * @return The date the item was checked in milliseconds since epoch
     */
    public @NotNull Long getCheckoutDate() {
        return checkoutDate;
    }

    /**
     * @param checkoutDate The date the item was checked in milliseconds since epoch
     */
    public void setCheckoutDate(@NotNull Long checkoutDate) {
        this.checkoutDate = checkoutDate;
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
    public @NotNull Boolean isActive() {
        return active;
    }

    /**
     * @param active Whether the item is currently checked out
     */
    public void setActive(@NotNull Boolean active) {
        this.active = active;
    }
}
