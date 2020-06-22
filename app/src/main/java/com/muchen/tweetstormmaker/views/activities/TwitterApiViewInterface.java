package com.muchen.tweetstormmaker.views.activities;

import com.muchen.tweetstormmaker.models.UserAndTokens;

public interface TwitterApiViewInterface {
    void adjustOptionsMenuItemVisibility(UserAndTokens u);
    void showToast(String msg, int duration);
    void showRedirectDialogFragment(String redirectUrl);
}
