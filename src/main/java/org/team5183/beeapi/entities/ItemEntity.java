package org.team5183.beeapi.entities;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

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

    @Column
    public @Nullable String checkout;

    @Column(nullable = false)
    public @NotNull String checkouts;

    private transient @Nullable CheckoutEntity checkoutEntity;

    private transient @NotNull Collection<CheckoutEntity> checkoutEntities;


    public ItemEntity(@NotNull String name, @NotNull String description, @NotNull String photo, @NotNull Double price, @Nullable String retailer, @Nullable String partNumber) {
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.price = price;
        this.retailer = retailer;
        this.partNumber = partNumber;
        this.checkoutEntities = new HashSet<>();
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    private ItemEntity() {
        new Gson().fromJson(checkout, CheckoutEntity.class);
        new Gson().fromJson(checkouts, CheckoutEntity[].class);
    }

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

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    public String getCheckouts() {
        return checkouts;
    }

    public void setCheckouts(String checkouts) {
        this.checkouts = checkouts;
    }

    public @Nullable CheckoutEntity getCheckoutEntity() {
        return checkoutEntity;
    }

    public void setCheckoutEntity(@Nullable CheckoutEntity checkoutEntity) {
        this.checkouts = new Gson().toJson(checkoutEntity);
        this.checkoutEntity = checkoutEntity;

        if (checkoutEntity != null) {
            addCheckout(checkoutEntity);
        } else {
            for (CheckoutEntity checkout : checkoutEntities) {
                if (checkout.getId().equals(checkoutEntity.getId()) && checkout.isActive()) {
                    checkout.setActive(false);
                    break;
                }
            }
        }
    }

    public @NotNull Collection<CheckoutEntity> getCheckoutEntities() {
        return checkoutEntities;
    }

    public @NotNull void addCheckout(@NotNull CheckoutEntity checkout) {
        this.checkoutEntities.add(checkout);
        this.checkouts = new Gson().toJson(checkoutEntities);
    }

    public void setCheckoutEntities(@NotNull Collection<CheckoutEntity> checkoutEntities) {
        this.checkouts = new Gson().toJson(checkoutEntities);
        this.checkoutEntities = checkoutEntities;
    }
}
