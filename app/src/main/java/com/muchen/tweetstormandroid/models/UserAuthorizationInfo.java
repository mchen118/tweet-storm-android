package com.muchen.tweetstormandroid.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "user_authorization_info")
public class UserAuthorizationInfo extends User{

    @ColumnInfo(name = "access_token")
    private String accessToken;

    @ColumnInfo(name = "access_token_secret")
    private String accessTokenSecret;

    public UserAuthorizationInfo() {}

    @Ignore
    public UserAuthorizationInfo(@NonNull String userId,
                                 @NonNull String screenName,
                                 @NonNull String profileImageURLHttps){
        super(userId, screenName, profileImageURLHttps);
    }

    @Ignore
    public UserAuthorizationInfo(@NonNull String userId,
                                 @NonNull String screenName,
                                 @NonNull String profileImageURLHttps,
                                 String accessToken,
                                 String accessTokenSecret){
        super(userId, screenName, profileImageURLHttps);
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

    public void setAccessToken(String token) { accessToken = token; }

    public String getAccessToken() { return accessToken; }

    public void setAccessTokenSecret(String secret) { accessTokenSecret = secret; }

    public String getAccessTokenSecret() { return accessTokenSecret; }
}
