package com.muchen.tweetstormmaker.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ActionProvider;

import com.muchen.tweetstormmaker.R;
import com.muchen.tweetstormmaker.concurrent.AppExecutorServices;
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
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View providerView = layoutInflater.inflate(R.layout.user_info_action_provider, null);
        Button logoutButton = (Button) providerView.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener((view)->{
            AppExecutorServices.soleInstance().diskIO().execute(()-> twitterApi.logout());
            userInfoMenuItem.collapseActionView();
        });
        TextView screenNameTextView = (TextView) providerView.findViewById(R.id.user_screen_name_text_view);
        screenNameTextView.setText("@" + screenName);
        return providerView;
    }

    @Override
    public View onCreateActionView(MenuItem item) {
        Log.d("debug.actionbar", "onCreateActionView(...) called");
        return onCreateActionView();
    }
}
