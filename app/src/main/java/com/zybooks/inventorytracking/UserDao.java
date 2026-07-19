package com.zybooks.inventorytracking;

import androidx.room.Dao;
import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
//TODO To be removed
// Data access object  for performing CRUD operations on users table
@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addUser(User user);

    @Query ("SELECT * FROM users WHERE id = :id")
    LiveData<User> getUser(long id);

    @Query("SELECT * FROM users ORDER BY username")
    LiveData<List<User>> getAllUsers();

    //Verifies credentials against db
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User getUser(String username, String password);

    // Ensures username does not already exist
    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUsername(String username);
}
