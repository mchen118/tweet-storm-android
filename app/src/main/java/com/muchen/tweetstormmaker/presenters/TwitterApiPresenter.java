package com.muchen.tweetstormmaker.presenters;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.muchen.tweetstormmaker.concurrent.AppExecutorServices;
import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.models.UserAndTokens;
import com.muchen.tweetstormmaker.restservice.TwitterApi;
import com.muchen.tweetstormmaker.restservice.TwitterApiException;
import com.muchen.tweetstormmaker.views.activities.TwitterApiViewInterface;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class TwitterApiPresenter implements TwitterApiPresenterInterface {
    private TwitterApiViewInterface view;
    private TwitterApi twitterApi;
    private AppExecutorServices executorServices;

    public TwitterApiPresenter(TwitterApiViewInterface view, TwitterApi twitterApi, AppExecutorServices executorServices) {
        this.view = view;
        this.twitterApi = twitterApi;
        this.executorServices = executorServices;
    }

    @Override
    public void getOrFetchUserInfo() {
        executorServices.networkIO().execute(()->{
            UserAndTokens u;
            if (twitterApi.getUserAndTokens() != null){
                // this avoids querying database if UserAndTokens exists in memory (i.e.
                // in the twitterApi instance)
                Log.d("debug.actionbar", "twitterApi.ua != null");
                u = twitterApi.getUserAndTokens();
            } else {
                Log.d("debug.actionbar", "twitterApi.ua == null");
                u = twitterApi.fetchUserAndTokens();
            }
            view.adjustOptionsMenuItemVisibility(u);
        });
    }

    private String retrieveAuthorizationURL(){
        Future<String> future = executorServices.diskIO().submit(()->{
            String futureResult;
            try {
                futureResult = twitterApi.retrieveAuthorizationURL();
            } catch (OAuthMessageSignerException |
                    OAuthNotAuthorizedException |
                    OAuthExpectationFailedException |
                    OAuthCommunicationException e) {
                Log.d("debug.signpost", e.toString());
                view.showToast("Attempt To Login Failed", Toast.LENGTH_LONG);
                return null;
            }
            return futureResult;
        });

        String url;
        try{
            url = future.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException |
                ExecutionException |
                TimeoutException e){
            Log.d("executorServices", e.toString());
            view.showToast("Attempt To Login Failed", Toast.LENGTH_LONG);
            return null;
        }
        return url;
    }

    @Override
    public void redirectToAuthorization() {
        String url = retrieveAuthorizationURL();
        if (url != null) { view.showRedirectDialogFragment(url); }
    }

    @Override
    public void setAndPersistUserAndTokens(String pin, Context appContext) {
        executorServices.networkIO().execute(()->{
            try {
                twitterApi.setUserAndTokens(pin, appContext);
            } catch (OAuthMessageSignerException | OAuthNotAuthorizedException |
                    OAuthExpectationFailedException | OAuthCommunicationException |
                    IOException e){
                Log.d("debug.signpost", e.toString());
                view.showToast("Attempt To Login Failed", Toast.LENGTH_LONG);
                return;
            }
            executorServices.diskIO().submit(()-> twitterApi.persistUserAndTokens());
        });
    }

    @Override
    public boolean tweetDraft(Draft draft) {
        Future<UserAndTokens> userAndTokensFuture =
                executorServices.diskIO().submit(()-> twitterApi.fetchUserAndTokens());
        UserAndTokens u;
        try {
            u = userAndTokensFuture.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException |
                ExecutionException |
                TimeoutException e){
            Log.d("debug.executorServices", e.toString());
            view.showToast("Encountered a thread execution error while " +
                            "accessing local data storage. Draft not sent.",
                    Toast.LENGTH_LONG);
            return false;
        }

        if ( u != null){
            Future<Boolean> future = executorServices.networkIO().submit(()->{
                if (twitterApi.getTwitterService() == null) { twitterApi.setTwitterService(); }
                try {
                    twitterApi.tweetDraft(draft);
                } catch (IOException e) {
                    view.showToast("Encountered a network error while tweeting - " +
                                    "a portion of your draft could have been partially sent.",
                            Toast.LENGTH_LONG);
                    Log.d("debug.network", e.toString());
                    return Boolean.FALSE;
                } catch (RuntimeException e){
                    view.showToast("Encountered an unexpected error while tweeting - " +
                                    "a portion of your draft could have been partially sent.",
                            Toast.LENGTH_LONG);
                    Log.d("debug.network", e.toString());
                    return Boolean.FALSE;
                } catch (TwitterApiException e){
                    view.showToast("Encountered a Twitter API error while tweeting - " +
                                    "a portion of your draft could have been partially sent.",
                            Toast.LENGTH_LONG);
                    Log.d("debug.twitterapi", e.toString());
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            });

            boolean result;
            try{
                result = future.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException |
                    ExecutionException |
                    TimeoutException e){
                Log.d("debug.executorServices", e.toString());
                view.showToast("Encountered a thread execution error while tweeting - " +
                                "a portion of your draft could have been tweeted out already.",
                        Toast.LENGTH_LONG);
                return false;
            }
            return result;

        } else {
            view.showToast("Please Login First", Toast.LENGTH_LONG);
            return false;
        }
    }
}
