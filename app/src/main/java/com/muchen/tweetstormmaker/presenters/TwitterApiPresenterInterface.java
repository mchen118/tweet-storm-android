package com.muchen.tweetstormmaker.presenters;

import android.content.Context;

import com.muchen.tweetstormmaker.models.Draft;

public interface TwitterApiPresenterInterface {
    void getOrFetchUserInfo();
    void redirectToAuthorization();
    void setAndPersistUserAndTokens(String pin, Context appContext);
    boolean tweetDraft(Draft draft);
}
