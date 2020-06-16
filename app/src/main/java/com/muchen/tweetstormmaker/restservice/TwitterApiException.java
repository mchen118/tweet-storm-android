package com.muchen.tweetstormmaker.restservice;

public class TwitterApiException extends Exception {
    public TwitterApiException (String twitterErrorMessage){
        super(twitterErrorMessage);
    }
}
