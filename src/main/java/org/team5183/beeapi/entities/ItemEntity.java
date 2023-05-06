package org.team5183.beeapi.entities;

import com.j256.ormlite.dao.ForeignCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bee_items")
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @NotNull Long id;

    @Column(nullable = false)
    private @NotNull String name;

    @Column(nullable = false)
    private @NotNull String description;

    @Column(nullable = false)
    private @NotNull String photo;

    @Column
    private @NotNull Double price;

    @Column
    private @Nullable String retailer;

    @Column
    private @Nullable String partNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "checkout_id", referencedColumnName = "id")
    private @Nullable CheckoutEntity checkout;

    @OneToMany(targetEntity = CheckoutEntity.class, mappedBy = "item")
    private @NotNull Collection<CheckoutEntity> checkouts;


    public ItemEntity(@NotNull String name, @NotNull String description, @NotNull String photo, @NotNull Double price, @Nullable String retailer, @Nullable String partNumber) {
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.price = price;
        this.retailer = retailer;
        this.partNumber = partNumber;
        this.checkouts = new HashSet<>();
    }

    @Deprecated
    public ItemEntity() {}

    public @NotNull Long getId() {
        return id;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public @NotNull String getPhoto() {
        return photo;
    }

    public void setPhoto(@NotNull String photo) {
        this.photo = photo;
    }

    public @NotNull Double getPrice() {
        return price;
    }

    public void setPrice(@NotNull Double price) {
        this.price = price;
    }

    public @Nullable String getRetailer() {
        return retailer;
    }

    public void setRetailer(@Nullable String retailer) {
        this.retailer = retailer;
    }

    public @Nullable String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(@Nullable String partNumber) {
        this.partNumber = partNumber;
    }

    public @Nullable CheckoutEntity getCheckout() {
        return checkout;
    }

    public void setCheckout(@Nullable CheckoutEntity checkout) {
        this.checkout = checkout;
    }

    public @NotNull Collection<CheckoutEntity> getCheckouts() {
        return checkouts;
    }

    public void setCheckouts(@NotNull Collection<CheckoutEntity> checkouts) {
        this.checkouts = checkouts;
    }
}
