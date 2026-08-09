package com.zybooks.inventorytracking;


import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

public class ItemDiffCallback extends DiffUtil.Callback {
    private final List<InventoryItem> oldList;
    private final List<InventoryItem> newList;

    public ItemDiffCallback(List<InventoryItem> oldList, List<InventoryItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() { return oldList.size(); }

    @Override
    public int getNewListSize() { return newList.size(); }

    @Override
    public boolean areItemsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).getId().equals(newList.get(newPos).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).getUpdatedAt().equals(newList.get(newPos).getUpdatedAt());
    }
}