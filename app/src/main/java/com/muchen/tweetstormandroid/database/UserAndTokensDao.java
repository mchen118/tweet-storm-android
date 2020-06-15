package com.muchen.tweetstormandroid.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.muchen.tweetstormandroid.models.UserAndTokens;

@Dao
public interface UserAndTokensDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserAuthorizationInfo(UserAndTokens ua);

    // If no primary key matches, this method doesn't change the DB.
    @Update
    void updateUserAuthorizationInfo(UserAndTokens ua);

    @Query("DELETE FROM user_and_tokens")
    void deleteUserAuthorizationInfo();

    // If return type is List<UserAndTokens>, this method
    // does not return null - an empty list is returned in case result set is empty.
    // If return type is UserAndTokens, it will return null.
    @Query("SELECT * FROM user_and_tokens")
    UserAndTokens fetchUserAuthorizationInfo();
}
