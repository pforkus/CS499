package com.zybooks.inventorytracking;

import java.util.List;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // GET items with optional search/filter/sort/pagination
    @GET("api/items")
    Call<ItemsResponse> getItems(
            @Query("search") String search,
            @Query("category") String category,
            @Query("sort") String sort,
            @Query("order") String order,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    // POST creates item
    @POST("api/items")
    Call<InventoryItem> createItem(@Body InventoryItem item);

    // GET categories
    @GET("api/items/categories")
    Call<List<String>> getCategories();

    // GET single item
    @GET("api/items/{id}")
    Call<InventoryItem> getItem(@Path("id") String id);

    // DELETE a single item
    @DELETE("api/items/{id}")
    Call<Void> deleteItem(@Path("id") String id);

    // PUT update item
    @PUT("api/items/{id}")
    Call<InventoryItem> updateItem(@Path("id") String id, @Body InventoryItem item);

    // POST delete many
    @POST("api/items/delete-many")
    Call<DeleteResponse> deleteItems(@Body DeleteRequest request);
}
