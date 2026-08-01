package com.zybooks.inventorytracking;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserViewModel extends AndroidViewModel {
    private final InventoryRepository mRepository;
    private final ServerRepository mServerRepository;
    private final MutableLiveData<User> mUser = new MutableLiveData<>();
    private final MutableLiveData<String> mError = new MutableLiveData<>();


    public UserViewModel(@NonNull Application application) {
        super(application);
        mRepository = InventoryRepository.getInstance(
                application.getApplicationContext()
        );
        mServerRepository = ServerRepository.getInstance(application);
    }

    public LiveData<ServerRepository.ServerState> getServerState() {
        return mServerRepository.getState();
    }
    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<String> getError() {
        return mError;
    }


    public void login(String username, String password) {
        mRepository.login(username, password, user -> {
            if(user != null) {
                mUser.postValue(user);
            } else {
                mError.postValue("Invalid username or password");
            }
        });
    }

    public void createUser(String username, String password) {
        mRepository.createUser(username, password, user -> {
            if(user != null) {
                mUser.postValue(user);
            } else {
                mError.postValue("Username already exists!");
            }
        });
    }

    public void logout() {
        mRepository.logout();
    }
}
