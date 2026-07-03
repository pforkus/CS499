package com.zybooks.inventorytracking;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
// Data Access Object for performing CRUD operations on items table
@Dao
public interface InventoryItemDao {
   @Insert
   long addItem(InventoryItem item);

   @Query("SELECT * FROM items WHERE id = :id")
   LiveData<InventoryItem> getItem(long id);

   @Query("SELECT * FROM items")
   LiveData<List<InventoryItem>> getAllItems();

   @Update
   void updateItem(InventoryItem item);

   @Delete
   void deleteItem(InventoryItem item);
}
