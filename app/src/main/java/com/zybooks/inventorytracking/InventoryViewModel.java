package com.zybooks.inventorytracking;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryViewModel extends AndroidViewModel {
    private final InventoryRepository mRepository;
    private final MutableLiveData<List<InventoryItem>> mAllItems = new MutableLiveData<>();
    private final MutableLiveData<List<String>> mCategories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>(false);
    private final Set<String> mSelectedCategories = new HashSet<>();
    private final Trie mNameTrie = new Trie();

    // Current query state
    private String mCurrentSearch = null;
    private String mCurrentSort = null;
    private String mCurrentOrder = null;
    private int mCurrentPage = 1;
    private int mTotalPages = 1;
    private static final int PAGE_SIZE = 20;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        mRepository = InventoryRepository.getInstance(application.getApplicationContext());
        refreshItems();
        loadCategories();
        loadTrieData();
    }

    public LiveData<List<InventoryItem>> getAllItems() {
        return mAllItems;
    }
    public LiveData<Boolean> getIsLoading() { return mIsLoading; }
    public LiveData<List<String>> getCategories() {
        return mCategories;
    }

    public boolean hasMorePages() {
        return mCurrentPage < mTotalPages;
    }

    public void loadNextPage() {
        if(Boolean.TRUE.equals(mIsLoading.getValue()) || !hasMorePages()) return;
        mCurrentPage++;
        fetchCurrentQuery(false);
    }

    private void startNewQuery() {
        mCurrentPage = 1;
        mTotalPages = 1;
        fetchCurrentQuery(true);
    }

    private void fetchCurrentQuery(boolean isFreshQuery) {
        mIsLoading.setValue(true);
        String categoryParam = mSelectedCategories.isEmpty()
                ? null
                : String.join(",", mSelectedCategories);

        mRepository.getItems(mCurrentSearch, categoryParam, mCurrentSort, mCurrentOrder, mCurrentPage, PAGE_SIZE,
                (items, pagination) -> {
            mIsLoading.postValue(false);
            if(pagination != null) {
                mTotalPages = pagination.getPages();
            }
            if (isFreshQuery) {
                mAllItems.postValue(items);
            }else {
                List<InventoryItem> current = new ArrayList<>(
                        mAllItems.getValue() != null
                                ? mAllItems.getValue()
                                : new ArrayList<>());
                current.addAll(items);
                mAllItems.postValue(current);
            }
                });
    }

    public void addItem(InventoryItem item, InventoryRepository.OnResultCallback externalCallback) {
        mRepository.addItem(item, success -> {
            if (success) {
                mNameTrie.insert(item.getName()); // Insert new name into Trie
                refreshItems(); // To show changed data
            }
            if (externalCallback != null) {
                externalCallback.onResult(success);
            }
        });
    }

    public void updateItem(InventoryItem item, InventoryRepository.OnResultCallback externalCallback) {
        mRepository.updateItem(item, success -> {
            if (success) {
                refreshItems();
            }
            if (externalCallback != null) {
                externalCallback.onResult(success);
            }
        });
    }

    public void deleteItem(InventoryItem item, InventoryRepository.OnResultCallback externalCallback) {
        mRepository.deleteItem(item, success -> {
            if (success) {
                mNameTrie.delete(item.getName()); // Delete name from Trie
                refreshItems();
            }
            if (externalCallback != null) {
                externalCallback.onResult(success);
            }
        });
    }

    public void deleteItems(List<InventoryItem> items) {
        mRepository.deleteItems(items, success -> {
            if (success) {
                for(InventoryItem item : items) {
                    mNameTrie.delete(item.getName());
                }
                refreshItems();
            }
        });
    }

    private void loadCategories() {
        mRepository.getCategories(mCategories::postValue);
    }


    public void toggleCategory(String category, boolean isSelected) {
        if(isSelected){
            mSelectedCategories.add(category);
        } else {
            mSelectedCategories.remove(category);
        }
        refreshItems();
    }

    public void search(String query) {
        mCurrentSearch = query;
        startNewQuery();
    }

    public void clearSearch() {
        mCurrentSearch = null;
        startNewQuery();
    }

    public void sort(String sortField, String sortOrder) {
        mCurrentSort = sortField;
        mCurrentOrder = sortOrder;
        startNewQuery();
    }

    private void refreshItems() {
        startNewQuery();
    }

    // Populates the trie with the list of all names
    public void populateTrie(List<String> allNames) {
        for(String name : allNames) {
            mNameTrie.insert(name);
        }
    }

    // Returns a list of items matching the prefix entered
    public List<String> getSuggestions(String prefix) {
        return mNameTrie.getSuggestions(prefix);
    }

    // Retrieves the list of all item names and populates trie once they arrive
    public void loadTrieData() {
        mRepository.getAllNames(names -> {
            Log.d("TRIE_DEBUG", "Names loaded: " + names.size());
            populateTrie(names);
        });
    }
}
