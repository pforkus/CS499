package com.zybooks.inventorytracking;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Represents a single inventory item (network/JSON model, matches Mongo Item schema)
public class InventoryItem implements Serializable {

    @SerializedName("_id")
    private String mId;

    private String sku;

    private String name;

    private long quantity;

    private String description;

    private Double price;

    private String imageUrl;

    private String category;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public InventoryItem() {
    }

    // Getters and setters
    public String getId() { return mId; }
    public void setId(String id) { mId = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
}