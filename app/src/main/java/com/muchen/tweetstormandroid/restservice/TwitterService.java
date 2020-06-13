package com.muchen.tweetstormandroid.restservice;

import com.muchen.tweetstormandroid.models.StatusId;
import com.muchen.tweetstormandroid.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface TwitterService {

    @GET("account/verify_credentials.json")
    Call<User> getUserInfo();

    @FormUrlEncoded
    @POST("statuses/update.json")
    Call<StatusId> postTweet(@Field("status") String status);

    @FormUrlEncoded
    @POST("statuses/update.json")
    Call<StatusId> replyToTweet(@FieldMap Map<String, String> options);
}
