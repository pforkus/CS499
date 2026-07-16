package com.zybooks.inventorytracking;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Represents a single inventory item and corresponding room database entity
@Entity(tableName = "items")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;
    @NonNull
    @ColumnInfo(name = "name")
    private String mName;
    @ColumnInfo(name = "quantity")
    private long mQuantity;
    @ColumnInfo(name = "image_path")
    private String mImagePath;


    public InventoryItem(String name, long quantity, String imagePath){
        this.mName = name;
        this.mQuantity = quantity;
        this.mImagePath = imagePath;

    }

    // Getters and setters
    public long getId() { return mId; }
    public void setId(long id) { mId = id; }

    public String getName() {return mName;}
    public void setName(String name) { mName = name; }

    public long getQuantity() {return mQuantity;}
    public void setQuantity(long quantity) { mQuantity = quantity; }

    public String getImagePath() {return mImagePath;}
    public void setImagePath(String imagePath) { mImagePath = imagePath; }
}
