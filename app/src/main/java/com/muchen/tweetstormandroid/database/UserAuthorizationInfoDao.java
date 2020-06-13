package com.muchen.tweetstormandroid.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.muchen.tweetstormandroid.models.UserAuthorizationInfo;

@Dao
public interface UserAuthorizationInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserAuthorizationInfo(UserAuthorizationInfo ua);

    // If no primary key matches, this method doesn't change the DB.
    @Update
    void updateUserAuthorizationInfo(UserAuthorizationInfo ua);

    @Query("DELETE FROM user_authorization_info")
    void deleteUserAuthorizationInfo();

    // If return type is List<UserAuthorizationInfo>, this method
    // does not return null - an empty list is returned in case result set is empty.
    // If return type is UserAuthorizationInfo, it will return null.
    @Query("SELECT * FROM user_authorization_info")
    UserAuthorizationInfo fetchUserAuthorizationInfo();
}
