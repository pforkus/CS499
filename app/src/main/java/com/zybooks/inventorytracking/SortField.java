package com.zybooks.inventorytracking;

// Stores fields to sort by
public enum SortField {
    NAME("name"),
    CATEGORY("category"),
    PRICE("price"),
    QUANTITY("quantity"),
    CREATED("createdAt"),
    UPDATE("updatedAt");

    private final String apiValue;

    SortField(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

}
