package com.zybooks.inventorytracking;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Represents a user account and its corresponding room database entity
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;

    @NonNull
    @ColumnInfo(name = "username")
    private String mUsername;

    @NonNull
    @ColumnInfo(name = "password")
    private String mPassword;

    //Constructor
    public User(String username, String password){
        mUsername = username;
        mPassword = password;
    }

    //Getters and Setters
    public long getId() { return mId; }
    public void setId(long id) { mId = id; }

    public String getUsername() { return mUsername; }
    public void setUsername(String username) { mUsername = username; }

    public String getPassword() { return mPassword; }
    public void setPassword(String password) { mPassword = password; }
}
