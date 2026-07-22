package com.zybooks.inventorytracking;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Represents a user account

public class User implements Serializable {

    @SerializedName("_id")
    private String mId;
    private String mUsername;

    public String getmUsername() {
        return mUsername;
    }

    public String getmId() {
        return mId;
    }
}
