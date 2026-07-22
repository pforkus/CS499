package com.zybooks.inventorytracking;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class InventoryApp  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Applies env configurations
        Map<String,String> config = new HashMap<>();
        config.put("cloud_name","hzbrxlwx");
        MediaManager.init(this, config);
    }
}
