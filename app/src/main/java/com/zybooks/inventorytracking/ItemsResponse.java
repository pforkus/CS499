package com.zybooks.inventorytracking;

import java.util.List;

public class ItemsResponse {
    private List<InventoryItem> items;
    private Pagination pagination;

    public List<InventoryItem> getItems() {
        return items;
    }
    public Pagination getPagination() {
        return pagination;
    }
}
