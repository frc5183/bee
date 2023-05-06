package org.team5183.beeapi.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;

@Entity
@Table(name = "bee_checkouts")
public class CheckoutEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public @NotNull Long id;

    @ManyToOne
    @JoinColumn(name="item_id", nullable = false)
    @NotNull ItemEntity item;

    @Column
    public @NotNull String checkoutName;

    @Column
    public @NotNull Long checkoutDate;

    @Column
    public @Nullable Long returnDate;

    @Column
    public @NotNull Boolean active;

    public CheckoutEntity(@NotNull ItemEntity item, @NotNull String checkoutName, @NotNull Long checkoutDate) {
        this.item = item;
        this.checkoutName = checkoutName;
        this.checkoutDate = checkoutDate;
        this.active = true;
    }

    @Deprecated
    public CheckoutEntity() {}

    public @NotNull Long getId() {
        return id;
    }

    public ItemEntity getItem() {
        return item;
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
