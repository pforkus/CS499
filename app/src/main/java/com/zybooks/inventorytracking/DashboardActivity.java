package com.zybooks.inventorytracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class DashboardActivity extends BaseActivity {

    private InventoryViewModel mViewModel;
    private InventoryAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mRecyclerView;
    private DrawerLayout mDrawLayout;
    private SelectionModeController mSelectionModeController;
    private static final int COLUMN_COUNT = 3;
    private boolean mIsGridView = true;
    private String mLastAppliedTextSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupToolbar();
        setupDrawer();
        setupWindowInsets();
        setupRecyclerView();
        setupFab();
        setupViewModel();
        setupClickListener();
        setupSelectionListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentSize = TextSizePrefs.get(this);
        if (mLastAppliedTextSize == null) {
            mLastAppliedTextSize = currentSize;
        } else if (!mLastAppliedTextSize.equals(currentSize)) {
            mLastAppliedTextSize = currentSize;
            recreate();
        }
    }

    // Creates click listener for recycler view cells
    // When a cell is clicked, opens ItemDialogFragment for selected item
    private void setupClickListener() {
        mAdapter.setOnItemClickListener(item ->{

            ItemDialogFragment dialog = ItemDialogFragment.newInstance(item);

            // Listen for results from dialog - save/delete actions
            dialog.setOnDialogResultListener(new ItemDialogFragment.OnDialogResultListener() {
                @Override
                public void onItemSaved(InventoryItem item, ItemDialogFragment.OnActionCompleteCallback callback) {

                    mViewModel.updateItem(item, callback::onComplete);
                }

                @Override
                public void onItemDeleted(InventoryItem item, ItemDialogFragment.OnActionCompleteCallback callback) {
                    mViewModel.deleteItem(item, callback::onComplete);
                }
            });
            dialog.show(getSupportFragmentManager(), "ItemDialog");
        });
    }

    // Sets up SelectionModeController, allows users to multi-select items and batch delete
    private void setupSelectionListener() {

        mSelectionModeController = new SelectionModeController(
                this,
                mAdapter,
                R.menu.selection_menu,
                ((menuItemId, selectedItems) -> {
                    if(menuItemId == R.id.action_delete) {
                        mViewModel.deleteItems(selectedItems);
                    }
                })
        );

        // Exit Action Mode if nothing is selected
        mAdapter.setOnSelectionStateChangedListener(() -> {
            if (mAdapter.selection.isActive()) {
                mSelectionModeController.start();
                mSelectionModeController.updateTitle();
            } else {
                mSelectionModeController.finish();
            }
        });
    }

    private void setupViewModel() {
        // Sets the viewmodel
        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Observe the item list and update adapter when data changes
        mViewModel.getAllItems().observe(this, items -> mAdapter.setItems(items));
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);

        // Click listener for new inventory item dialog
        fab.setOnClickListener(v -> {
            // Pass null here to indicate this is a new item with no data to populate fields
            ItemDialogFragment dialog = ItemDialogFragment.newInstance(null);

            // Listen for results from dialog
            dialog.setOnDialogResultListener(new ItemDialogFragment.OnDialogResultListener() {
                @Override
                public void onItemSaved(InventoryItem savedItem, ItemDialogFragment.OnActionCompleteCallback callback) {
                    mViewModel.addItem(savedItem, callback::onComplete);
                }

                @Override
                public void onItemDeleted(InventoryItem deletedItem, ItemDialogFragment.OnActionCompleteCallback callback) {
                    // N.A

                }
            });
            dialog.show(getSupportFragmentManager(), "ItemDialog");
        });
    }

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        // Sets up the recycler view layout options
        mGridLayoutManager = new GridLayoutManager(this, COLUMN_COUNT);
        mLinearLayoutManager = new LinearLayoutManager(this);

        // Sets default layout manager
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        // Creates an adapter and connects to recyclerview
        mAdapter = new InventoryAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupWindowInsets() {
        // Adjust padding to avoid content being hidden behind system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
    }

    private void setupDrawer() {
        mDrawLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this::handleDrawerItemClick);
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        assert searchView != null;
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setBackground(ContextCompat.getDrawable(this, R.drawable.search_field_background));
        return true;
    }


    // Handles Toolbar item selections, manages layout view and menu selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Opens menu
        if(id == R.id.action_menu) {
            mDrawLayout.openDrawer(GravityCompat.END);
            return true;
        }

        // Toggles grid view and list view
        if(id == R.id.action_toggle_layout) {
            toggleLayoutManager();
            item.setIcon(mIsGridView ? R.drawable.ic_grid_view : R.drawable.ic_list_view);
            return true;
        }

        if (id == R.id.action_sort) {
            new SortBottomSheet().show(getSupportFragmentManager(), "sort_sheet");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Handles menu selections
    private boolean handleDrawerItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_settings) { // Starts Settings Activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) { // Returns to the Login Activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) { // Exits the application
            finishAffinity();
        }
        mDrawLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    // Checks value to toggle between grid and list view
    private void toggleLayoutManager() {
        mIsGridView = !mIsGridView;
        mRecyclerView.setLayoutManager(mIsGridView ? mGridLayoutManager : mLinearLayoutManager);
    }

}
