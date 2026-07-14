package com.zybooks.inventorytracking;

import java.util.HashSet;
import java.util.Set;

public class SelectionTracker<T> {
    private final Set<T> selectedIds = new HashSet<>();
    private boolean active = false;

    public boolean toggle(T id) {
        if(selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        active = !selectedIds.isEmpty();
        return selectedIds.contains(id);
    }

    public boolean isSelected(T id) {
        return selectedIds.contains(id);
    }

    public Set<T> selectedIds() {
        return new HashSet<>(selectedIds);
    }

    public boolean isActive() {
        return active;
    }

    public void clear() {
        selectedIds.clear();
        active = false;
    }
}
