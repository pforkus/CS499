package com.zybooks.inventorytracking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Represents a user account

public class User implements Serializable {

    @SerializedName("_id")
    private String mId;
    @SerializedName("username")
    private String mUsername;
    @SerializedName("token")
    private String mToken;

    public String getmUsername() {
        return mUsername;
    }

    public String getmId() {
        return mId;
    }

    public String getmToken() { return mToken; }
}
