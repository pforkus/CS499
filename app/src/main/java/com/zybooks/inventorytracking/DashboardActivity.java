package com.zybooks.inventorytracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



public class DashboardActivity extends BaseActivity {

    private InventoryViewModel mViewModel;
    private UserViewModel mUserViewModel;
    private InventoryAdapter mAdapter;
    private SuggestionAdapter mSuggestionAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mRecyclerView;
    private RecyclerView mSuggestionRecyclerView;
    private DrawerLayout mDrawLayout;
    private SelectionModeController mSelectionModeController;
    private ChipGroup mCategoryChipGroup;
    private SearchView mSearchView;
    private static final int COLUMN_COUNT = 2;

    private boolean mIsGridView = true;
    private String mLastAppliedTextSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mCategoryChipGroup = findViewById(R.id.categoryChipGroup);

        setupToolbar();
        setupDrawer();
        setupWindowInsets();
        setupRecyclerView();
        setupSuggestionsRecyclerView();
        setupFab();
        setupViewModel();
        setupClickListener();
        setupSelectionListener();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks if the font size matches last applied size,
        // if different, rerenders activity to push changes
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
        // Sets the inventory viewmodel
        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // User ViewModel
        mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Sets inventory viewmodel to observe the item list and update adapter when data changes
        mViewModel.getAllItems().observe(this, items -> mAdapter.setItems(items));
        mViewModel.getCategories().observe(this, this::populateCategoryChips);
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

    // Sets up main recyclerview used to display inventory items as cards
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

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if(dy <= 0) return;
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if(lm == null) return;

                int visibleItemCount = lm.getChildCount();
                int totalItemCount = lm.getItemCount();
                int firstVariable = lm.findFirstVisibleItemPosition();
                final int REFRESH_BUFFER = 4;

                if((visibleItemCount + firstVariable) >= totalItemCount - REFRESH_BUFFER) {
                    mViewModel.loadNextPage();
                }
            }
        });
    }

    private void setupSuggestionsRecyclerView() {
        mSuggestionRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        mSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSuggestionAdapter = new SuggestionAdapter(name -> {
            mSearchView.setQuery(name, false);
            mViewModel.search(name);
            dismissSuggestions();
            mSuggestionRecyclerView.setVisibility(View.GONE);
        });
        mSuggestionRecyclerView.setAdapter(mSuggestionAdapter);
    }

    private void dismissSuggestions() {
        mSuggestionAdapter.submitList(new ArrayList<>());
        mSuggestionRecyclerView.setVisibility(View.GONE);
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

    private void populateCategoryChips(List<String> categories) {
        mCategoryChipGroup.removeAllViews();

        for(String category: categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setClickable(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) ->
                    mViewModel.toggleCategory(category, isChecked));
            mCategoryChipGroup.addView(chip);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        setupSearch(menu);

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

        // Opens sort bottom sheet
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
            // Remove token
            mUserViewModel.logout();

            // Return to login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_exit) { // Exits the application
            finishAffinity();
        }
        mDrawLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    // Handles search and clearing search
    private void setupSearch(Menu menu) {

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();

        View searchPlate = Objects.requireNonNull(mSearchView)
                .findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setBackground(
                ContextCompat.getDrawable(this, R.drawable.search_field_background));
                EditText searchEditText = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
                searchEditText.setTextColor(ContextCompat.getColor(this, R.color.forest_green_dark));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()) {
                    mViewModel.clearSearch();
                    dismissSuggestions();
                } else {
                    List<String> suggestions = mViewModel.getSuggestions(newText);
                    mSuggestionAdapter.submitList(suggestions);
                    mSuggestionRecyclerView.setVisibility(
                            suggestions.isEmpty() ? View.GONE : View.VISIBLE
                    );
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.search(query);
                dismissSuggestions();
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                mSearchView.setQuery("", false);
                mViewModel.clearSearch();
                return true;

            }
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                return true;
            }
        });
    }

    // Checks value to toggle between grid and list view
    private void toggleLayoutManager() {
        mIsGridView = !mIsGridView;
        mRecyclerView.setLayoutManager(mIsGridView ? mGridLayoutManager : mLinearLayoutManager);
        mAdapter.setGridView(mIsGridView);
    }
}
