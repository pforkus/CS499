package com.zybooks.inventorytracking;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;

public class DashboardActivity extends AppCompatActivity {

    private InventoryViewModel mViewModel;
    private InventoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Adjust padding to avoid content being hidden behind system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);

        // Sets the recycler view layout to a grid with 3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        mAdapter = new InventoryAdapter();
        recyclerView.setAdapter(mAdapter);

        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Observe the item list and update adapter when data changes
        mViewModel.getAllItems().observe(this, items -> {
            mAdapter.setItems(items);
        });

        // Creates click listener for recycler view cells
        // When a cell is clicked, opens ItemDialogFragment for selected item
        mAdapter.setOnItemClickListener(item ->{
            ItemDialogFragment dialog = ItemDialogFragment.newInstance(item);

            // Listen for results from dialog - save/delete actions
            dialog.setOnDialogResultListener(new ItemDialogFragment.OnDialogResultListener() {
                @Override
                public void onItemSaved(InventoryItem item) {
                    mViewModel.updateItem(item);
                }

                @Override
                public void onItemDeleted(InventoryItem item) {
                    mViewModel.deleteItem(item);
                }
            });
            dialog.show(getSupportFragmentManager(), "ItemDialog");
        });

        // Creates click listener for delete button - opens confirmation dialog when pressed
        mAdapter.setOnDeleteClickListener(item -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete", (d, which) -> {
                        mViewModel.deleteItem(item);
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();

            // Set button colors explicitly for contrast
            int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        });

        // Open dialog to create new inventory item
        fab.setOnClickListener(v -> {
            // Pass null here to indicate this is a new item with no data to populate fields
            ItemDialogFragment dialog = ItemDialogFragment.newInstance(null);

            // Listen for results from dialog
            dialog.setOnDialogResultListener(new ItemDialogFragment.OnDialogResultListener() {
                @Override
                public void onItemSaved(InventoryItem savedItem) {
                    mViewModel.addItem(savedItem);
                }

                @Override
                public void onItemDeleted(InventoryItem deletedItem) {
                // Not applicable for creating new items
                }
            });
            dialog.show(getSupportFragmentManager(), "ItemDialog");
        });
    }
}
