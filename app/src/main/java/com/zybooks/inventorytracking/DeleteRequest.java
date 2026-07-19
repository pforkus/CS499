package com.zybooks.inventorytracking;

import java.util.List;

public class DeleteRequest {
    private final List<String> ids;

    public DeleteRequest(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getIds() {
        return ids;
    }
}