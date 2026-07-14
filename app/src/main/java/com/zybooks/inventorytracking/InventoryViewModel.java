package com.zybooks.inventorytracking;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.zybooks.inventorytracking.InventoryItem;
import com.zybooks.inventorytracking.InventoryRepository;
import java.util.List;

// ViewModel for dashboard, survives config changes and
// exposes inventory data and operations to the UI via repository
public class InventoryViewModel extends AndroidViewModel {
    private InventoryRepository mRepository;
    private LiveData<List<InventoryItem>> mItems;

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        mRepository = InventoryRepository.getInstance(application.getApplicationContext());
    }

    public LiveData<List<InventoryItem>> getAllItems(){
        return mRepository.getAllItems();
    }

    public LiveData<InventoryItem> getItem(long id) {
        return mRepository.getItem(id);
    }

    public void addItem(InventoryItem item) { mRepository.addItem(item); }

    public void updateItem(InventoryItem item) {
        mRepository.updateItem(item);
    }

    public void deleteItem(InventoryItem item) {
        mRepository.deleteItem(item);
    }

    public void deleteItems(List<InventoryItem> items) { mRepository.deleteItems(items);}
}
