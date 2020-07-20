package com.muchen.tweetstormmaker.views.actionproviders;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.core.view.ActionProvider;

import com.muchen.tweetstormmaker.concurrent.AppExecutorServices;
import com.muchen.tweetstormmaker.databinding.UserInfoActionProviderBinding;
import com.muchen.tweetstormmaker.restservice.TwitterApi;

public class UserInfoActionProvider extends ActionProvider {
    private Context context;
    private String screenName;
    private TwitterApi twitterApi;
    private MenuItem userInfoMenuItem;

    public UserInfoActionProvider(Context context,
                                  String screenName,
                                  TwitterApi twitterApi,
                                  MenuItem userInfoMenuItem) {
        super(context);
        this.context = context;
        this.screenName = screenName;
        this.twitterApi = twitterApi;
        this.userInfoMenuItem = userInfoMenuItem;
    }

    @Override
    public View onCreateActionView() {
        Log.d("debug.actionbar", "onCreateActionView() called");
        UserInfoActionProviderBinding binding = UserInfoActionProviderBinding.inflate(
                LayoutInflater.from(context));
        binding.logoutButton.setOnClickListener((view)->{
            AppExecutorServices.soleInstance().diskIO().execute(()-> twitterApi.logout());
            userInfoMenuItem.collapseActionView();
        });
        binding.userScreenNameTextView.setText("@" + screenName);
        return binding.getRoot();
    }

    @Override
    public View onCreateActionView(MenuItem item) {
        Log.d("debug.actionbar", "onCreateActionView(...) called");
        return onCreateActionView();
    }
}
