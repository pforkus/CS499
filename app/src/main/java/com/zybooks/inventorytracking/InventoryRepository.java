package com.zybooks.inventorytracking;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Singleton repo, manages database operations for app,
// providing a single access point for user and inventory data
public class InventoryRepository {
    private static InventoryRepository mInventoryRepo;
    private final ApiService mApiService;
    private final TokenManager mTokenManager;

    // Returns existing instance or creates one if it does not exist
    public static InventoryRepository getInstance(Context context) {
        if (mInventoryRepo == null) {
            mInventoryRepo = new InventoryRepository(context);
        }
        return mInventoryRepo;
    }

    private InventoryRepository (Context context) {
        mApiService = RetrofitClient.getInstance(context).create(ApiService.class);
        mTokenManager = new TokenManager(context.getApplicationContext());
    }

    // === User methods === // FIXME separate into own repository


    public interface OnUserCallback {
        void onResult(User user);
    }
    public void createUser(String username, String password, OnUserCallback callback) {
        UserRequest request = new UserRequest(username, password);

        mApiService.createUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("USER_API", "Response code: " + response.code());
                if(response.isSuccessful() && response.body() != null) {
                    mTokenManager.saveToken(response.body().getmToken()); // Extracts and saves token
                    callback.onResult(response.body());
                } else {
                    callback.onResult(null);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("USER_API", "Failed", t);
                callback.onResult(null);
            }
        });
    }

    public void login (String username, String password, OnUserCallback callback) {
        UserRequest request = new UserRequest(username, password);
        Log.d("LOGIN", "About to enqueue login request");

        mApiService.login(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful() && response.body() != null) {
                    mTokenManager.saveToken(response.body().getmToken()); // Extracts and saves token
                    callback.onResult(response.body());
                } else {
                    callback.onResult(null);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                    callback.onResult(null);
            }
        });
    }

    public void logout() {
        mTokenManager.logout();
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

    public interface OnNamesChangedCallback {
        void onNamesLoaded(List<String> names);
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

    public void getAllNames(OnNamesChangedCallback callback) {
        mApiService.getAllNames().enqueue(new Callback<List<String>>() {
           @Override
           public void onResponse(Call<List<String>> call, Response<List<String>> response) {
               if(response.isSuccessful() && response.body() != null) {
                   callback.onNamesLoaded(response.body());
               } else {
                   callback.onNamesLoaded(Collections.emptyList());
               }
           }
           @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
               callback.onNamesLoaded(Collections.emptyList());
           }
        });
    }
}
