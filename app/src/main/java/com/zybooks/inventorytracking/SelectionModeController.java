package com.zybooks.inventorytracking;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import java.util.List;

public class SelectionModeController {

    public interface OnActionListener {
        void onAction(int menuItemId, List<InventoryItem> selectedItems);
    }

    private final AppCompatActivity activity;
    private final InventoryAdapter adapter;
    @MenuRes private final int menuRes;
    private final OnActionListener onAction;
    private ActionMode actionMode;

    public SelectionModeController(AppCompatActivity activity,
                                   InventoryAdapter adapter,
                                   @MenuRes int menuRes,
                                   OnActionListener onAction) {
        this.activity = activity;
        this.adapter = adapter;
        this.menuRes = menuRes;
        this.onAction = onAction;
    }

    public void start() {
        if(actionMode != null) return;

        actionMode = activity.startSupportActionMode(new ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(menuRes, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                onAction.onAction(item.getItemId(), adapter.getSelectedItems());
                mode.finish(); // Exit after deletion
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelection();
                actionMode = null;

            }
        });
    }

    // Updates "n selected" on toolbar
    public void updateTitle() {
        if (actionMode != null) {
            actionMode.setTitle(adapter.selection.selectedIds().size() + " selected");
        }
    }

    public void finish() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }
}
