package com.zybooks.inventorytracking;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Singleton repo, manages database operations for app,
// providing a single access point for user and inventory data
public class InventoryRepository {
    private static InventoryRepository mInventoryRepo;
    private final UserDao mUserDao;
    private final ApiService mApiService;
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
                .fallbackToDestructiveMigration()
                .build();
        mUserDao = database.userDao();
        mApiService = RetrofitClient.getInstance().create(ApiService.class);

        //mInventoryItemDao = database.inventoryItemDao();

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

    // ** === Inventory item methods === ** //
    // ** ===                        === ** //

    public interface OnItemsLoadedCallback {
        void onItemsLoaded(List<InventoryItem> items);
    }

    public interface OnResultCallback {
        void onResult(boolean success);
    }

    public interface OnCategoriesLoadedCallback {
        void onCategoriesLoaded(List<String> categories);
    }

    public void getItems(String search, String category, String sort, String order, Integer page, Integer limit, OnItemsLoadedCallback callback) {
        mApiService.getItems(search, category, sort, order, page, limit).enqueue(new Callback<ItemsResponse>() {
            @Override
            public void onResponse(Call<ItemsResponse> call, Response<ItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onItemsLoaded(response.body().getItems());
                } else {
                    callback.onItemsLoaded(Collections.emptyList());
                }
            }
            @Override
            public void onFailure(Call<ItemsResponse> call, Throwable t) {
                callback.onItemsLoaded(Collections.emptyList());
            }
        });
    }

    public void addItem(InventoryItem item, OnResultCallback callback) {
        mApiService.createItem(item).enqueue(new Callback<InventoryItem>() {
            @Override
            public void onResponse(Call<InventoryItem> call, Response<InventoryItem> response) {
                callback.onResult(response.isSuccessful());
            }
            @Override
            public void onFailure(Call<InventoryItem> call, Throwable t) {
                callback.onResult(false);
            }
        });
    }

    public void updateItem(InventoryItem item, OnResultCallback callback) {
        mApiService.updateItem(item.getId(), item).enqueue(new Callback<InventoryItem>() {
            @Override
            public void onResponse(Call<InventoryItem> call, Response<InventoryItem> response) {
                callback.onResult(response.isSuccessful());
            }
            @Override
            public void onFailure(Call<InventoryItem> call, Throwable t) {
                callback.onResult(false);
            }
        });
    }

    public void deleteItem(InventoryItem item, OnResultCallback callback) {
        mApiService.deleteItem(item.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onResult(response.isSuccessful());
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onResult(false);
            }
        });
    }

    public void deleteItems(List<InventoryItem> items, OnResultCallback callback) {
        List<String> ids = new ArrayList<>();
        for (InventoryItem item : items) {
            ids.add(item.getId());
        }
        mApiService.deleteItems(new DeleteRequest(ids)).enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                callback.onResult(response.isSuccessful());
            }
            @Override
            public void onFailure(Call<DeleteResponse> call, Throwable t) {
                callback.onResult(false);
            }
        });
    }

    public void getCategories(OnCategoriesLoadedCallback callback) {
        mApiService.getCategories().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onCategoriesLoaded(response.body());
                } else {
                    callback.onCategoriesLoaded(Collections.emptyList());
                }
            }
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onCategoriesLoaded(Collections.emptyList());
            }
        });
    }
}
