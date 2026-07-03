package com.zybooks.inventorytracking;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Room database definition, includes user and inventoryitem tables
@Database(entities = {User.class, InventoryItem.class}, version = 1, exportSchema = false)
public abstract class InventoryDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract InventoryItemDao inventoryItemDao();
}
