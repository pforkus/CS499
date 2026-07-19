package com.zybooks.inventorytracking;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// Room database definition, includes user and inventoryitem tables
@Database(entities = {User.class}, version = 2, exportSchema = false)
public abstract class InventoryDatabase extends RoomDatabase {
    public abstract UserDao userDao();

}
