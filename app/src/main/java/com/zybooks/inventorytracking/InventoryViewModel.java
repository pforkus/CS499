package com.zybooks.inventorytracking;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class InventoryViewModel extends AndroidViewModel {
    private final InventoryRepository mRepository;
    private final MutableLiveData<List<InventoryItem>> mAllItems = new MutableLiveData<>();

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        mRepository = InventoryRepository.getInstance(application.getApplicationContext());
        refreshItems();
    }

    public LiveData<List<InventoryItem>> getAllItems() {
        return mAllItems;
    }

    public void addItem(InventoryItem item, InventoryRepository.OnResultCallback externalCallback) {
        mRepository.addItem(item, success -> {
            if (success) {
                refreshItems();
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
                refreshItems();
            }
        });
    }

    private void refreshItems() {
        mRepository.getAllItems(items -> mAllItems.postValue(items));
    }
}
