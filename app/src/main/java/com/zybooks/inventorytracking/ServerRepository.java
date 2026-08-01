package com.zybooks.inventorytracking;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRepository {

    private final ApiService mApiService;
    public enum ServerState { WAKING, READY, FAILED }
    private static ServerRepository INSTANCE;
    private final MutableLiveData<ServerState> state = new MutableLiveData<>(ServerState.WAKING);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ServerRepository(Context context) {
        mApiService = RetrofitClient.getInstance(context).create(ApiService.class);
    }

    public static synchronized ServerRepository getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new ServerRepository(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public LiveData<ServerState> getState() {
        return state;
    }

    public void wakeServer() {
        state.postValue(ServerState.WAKING);
        executor.execute(() -> {
            try {
                mApiService.ping().execute();
                state.postValue(ServerState.READY);
            } catch (Exception e) {
                state.postValue(ServerState.FAILED);
            }
        });
    }
}
