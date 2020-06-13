package com.muchen.tweetstormandroid.models;

import com.google.gson.annotations.SerializedName;

public class TwitterErrors {
    private Error[] errors;

    public Error[] getErrors() { return errors; }

    public void setErrors(Error[] errors) { this.errors = errors; }

    public static class Error {
        @SerializedName("code")
        private int twitterErrorCode;
        @SerializedName("message")
        private String twitterErrorMessage;

        public int getTwitterErrorCode() { return twitterErrorCode; }

        public void setTwitterErrorCode(int twitterErrorCode) { this.twitterErrorCode = twitterErrorCode; }

        public String getTwitterErrorMessage() { return twitterErrorMessage; }

        public void setTwitterErrorMessage(String twitterErrorMessage) { this.twitterErrorMessage = twitterErrorMessage; }

        public Error() {}
    }

    public TwitterErrors(){}
}
