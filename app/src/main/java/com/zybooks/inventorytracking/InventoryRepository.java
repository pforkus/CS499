package com.zybooks.inventorytracking;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Singleton repo, manages database operations for app,
// providing a single access point for user and inventory data
public class InventoryRepository {
    private static InventoryRepository mInventoryRepo;
    private final UserDao mUserDao;
    private final InventoryItemDao mInventoryItemDao;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    // Returns existing instance or creates one if it does not exist
    public static InventoryRepository getInstance(Context context) {
        if (mInventoryRepo == null) {
            mInventoryRepo = new InventoryRepository(context);
        }
        return mInventoryRepo;
    }

    private InventoryRepository (Context context) {
        InventoryDatabase database = Room.databaseBuilder(context, InventoryDatabase.class, "inventory.db")
                .build();
        mUserDao = database.userDao();
        mInventoryItemDao = database.inventoryItemDao();

    }

    // === User methods ===
    public void addUser(User user, OnUserAddedCallback callback) {
        mExecutor.execute(() -> {
            long userId = mUserDao.addUser(user);
            user.setId(userId); // Assigns generated ID back to user obj
            if (callback != null) {
                callback.onUserAdded(user);
            }
        });
    }

    public interface OnUserAddedCallback {
        void onUserAdded(User user);
    }
    public interface OnUserFetchedCallback {
        void onUserFetched(User user);
    }


    public void getUser (String username, String password, OnUserFetchedCallback callback) {
        mExecutor.execute(() -> {
            User user = mUserDao.getUser(username, password);
            if (callback != null) {
                callback.onUserFetched(user);
            }
        });

    }

    public void getUserByUsername(String username, OnUserFetchedCallback callback) {
        mExecutor.execute(() ->{
            User user = mUserDao.getUserByUsername(username);
            if (callback != null) {
                callback.onUserFetched(user);
            }
        });
    }

    public LiveData<List<User>> getAllUsers() {
        return mUserDao.getAllUsers();
    }

    // === Inventory item methods ===
    public void addItem(InventoryItem item) {
        mExecutor.execute(() -> {
            long itemId = mInventoryItemDao.addItem(item);
            item.setId(itemId); // Assigns generated ID back to item obj
        });
    }

    public LiveData<InventoryItem> getItem(long itemId) {
        return mInventoryItemDao.getItem(itemId);
    }

    public LiveData<List<InventoryItem>> getAllItems() {
        return mInventoryItemDao.getAllItems();
    }

    public void updateItem(InventoryItem item) {
        mExecutor.execute(() ->mInventoryItemDao.updateItem(item));
    }

    public void deleteItem(InventoryItem item) {
        mExecutor.execute(() ->mInventoryItemDao.deleteItem(item));
    }
}
