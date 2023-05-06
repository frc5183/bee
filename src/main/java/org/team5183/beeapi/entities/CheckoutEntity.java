package org.team5183.beeapi.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;

public class CheckoutEntity {
    private @NotNull Long id;


    private @NotNull String checkoutName;

    private @NotNull Long checkoutDate;

    private @Nullable Long returnDate;

    private @NotNull Boolean active;

    public CheckoutEntity(@NotNull String checkoutName, @NotNull Long checkoutDate) {
        this.checkoutName = checkoutName;
        this.checkoutDate = checkoutDate;
        this.active = true;
    }

    private CheckoutEntity() {}

    public @NotNull Long getId() {
        return id;
    }

    public @NotNull String getCheckoutName() {
        return checkoutName;
    }

    public void setCheckoutName(@NotNull String checkoutName) {
        this.checkoutName = checkoutName;
    }

    public @NotNull Long getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(@NotNull Long checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public @Nullable Long getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(@Nullable Long returnDate) {
        this.returnDate = returnDate;
    }

    public @NotNull Boolean isActive() {
        return active;
    }

    public void setActive(@NotNull Boolean active) {
        this.active = active;
    }
}
