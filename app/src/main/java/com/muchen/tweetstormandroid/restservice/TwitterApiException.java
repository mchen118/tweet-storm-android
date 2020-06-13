package com.muchen.tweetstormandroid.restservice;

public class TwitterApiException extends Exception {
    public TwitterApiException (String twitterErrorMessage){
        super(twitterErrorMessage);
    }
}
