package com.muchen.tweetstormandroid.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

public class User {
    @NonNull @SerializedName("id_str")
    @PrimaryKey @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull @SerializedName("screen_name")
    @ColumnInfo(name = "screen_name")
    private String screenName;

    @NonNull @SerializedName("profile_image_url_https")
    private String profileImageURLHttps;

    public User() {}

    @Ignore
    public User(@NonNull String userId,
                @NonNull String screenName,
                @NonNull String profileImageURLHttps){
        this.userId = userId;
        this.screenName = screenName;
        this.profileImageURLHttps = profileImageURLHttps;
    }

    public void setUserId(@NonNull String userId) { this.userId = userId; }

    @NonNull public String getUserId() { return userId;}

    public void setScreenName(@NonNull String screenName) { this.screenName = screenName; }

    @NonNull public String getScreenName() { return screenName; }

    public void setProfileImageURLHttps(@NonNull String profileImageURLHttps) { this.profileImageURLHttps = profileImageURLHttps; }

    @NonNull public String getProfileImageURLHttps() { return profileImageURLHttps; }
}
