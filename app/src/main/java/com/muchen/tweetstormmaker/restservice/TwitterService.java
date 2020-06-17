package com.muchen.tweetstormmaker.restservice;

import com.muchen.tweetstormmaker.models.StatusId;
import com.muchen.tweetstormmaker.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TwitterService {

    @GET("account/verify_credentials.json")
    Call<User> fetchUser();

    @FormUrlEncoded
    @POST("statuses/update.json")
    Call<StatusId> postTweet(@Field("status") String status);

    @FormUrlEncoded
    @POST("statuses/update.json")
    Call<StatusId> replyToTweet(@FieldMap Map<String, String> options);
}
