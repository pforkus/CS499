package com.zybooks.inventorytracking;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryViewModel extends AndroidViewModel {
    private final InventoryRepository mRepository;
    private final MutableLiveData<List<InventoryItem>> mAllItems = new MutableLiveData<>();
    private final MutableLiveData<List<String>> mCategories = new MutableLiveData<>();
    private final Set<String> mSelectedCategories = new HashSet<>();
    private final Trie mNameTrie = new Trie();

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

    public LiveData<List<String>> getCategories() {
        return mCategories;
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
        mRepository.getItems(query, null, null, null, null, null,
                mAllItems::postValue);
    }

    public void clearSearch() {
        refreshItems();
    }

    public void sort(String sortField, String sortOrder) {
        mRepository.getItems(null, null, sortField, sortOrder, null, null,
                mAllItems::postValue);
    }
    // Clear parameters besides set categories, retains previous category selection
    private void refreshItems() {
        String categoryParam = mSelectedCategories.isEmpty()
                ? null
                : String.join(",", mSelectedCategories);

        mRepository.getItems(null, categoryParam, null, null, null, null,
                mAllItems::postValue);
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
