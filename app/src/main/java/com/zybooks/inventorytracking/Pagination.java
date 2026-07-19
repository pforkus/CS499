package com.zybooks.inventorytracking;

public class Pagination {
    private int page;
    private int limit;
    private int total;
    private int pages;

    public int getPage() { return page; }

    public int getLimit() {
        return limit;
    }

    public int getTotal() {
        return total;
    }

    public int getPages() { return pages; }
}
