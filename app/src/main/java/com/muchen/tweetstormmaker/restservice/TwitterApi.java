package com.muchen.tweetstormmaker.restservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;

import com.muchen.tweetstormmaker.constants.Constants;
import com.muchen.tweetstormmaker.database.AppDatabase;
import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.models.StatusId;
import com.muchen.tweetstormmaker.models.TwitterErrors;
import com.muchen.tweetstormmaker.models.User;
import com.muchen.tweetstormmaker.models.UserAndTokens;

import com.twitter.twittertext.TwitterTextParseResults;
import com.twitter.twittertext.TwitterTextParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import okhttp3.OkHttpClient;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthProvider;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

public class TwitterApi {
    private AppDatabase db;
    private UserAndTokens ua;

    private OkHttpOAuthConsumer consumer;
    private OkHttpOAuthProvider provider;

    private TwitterService twitterServiceGson;

    private static final Object lock = new Object();
    private static volatile TwitterApi soleInstance;

    private TwitterApi(String CONSUMER_KEY, String CONSUMER_KEY_SECRET, AppDatabase db) {
        consumer = new OkHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
        provider = new OkHttpOAuthProvider(
                Constants.TWITTER_API_REQUEST_TOKEN_ENDPOINT_URL,
                Constants.TWITTER_API_ACCESS_TOKEN_ENDPOINT_URL,
                Constants.TWITTER_API_AUTHORIZE_ENDPOINT_URL
        );
        this.db = db;
    }

    public static TwitterApi soleInstance(String CONSUMER_KEY, String CONSUMER_KEY_SECRET, AppDatabase db){
        if (soleInstance == null){
            synchronized (lock){
                if (soleInstance == null){
                    soleInstance = new TwitterApi(CONSUMER_KEY, CONSUMER_KEY_SECRET, db);
                }
            }
        }
        return soleInstance;
    }

    public UserAndTokens fetchUserAuthorizationInfo(){
        ua = db.userAuthorizationInfoDao().fetchUserAuthorizationInfo();
        return ua;
    }

    public UserAndTokens getUserAuthorizationInfo() { return ua; }

    public TwitterService getTwitterService() { return twitterServiceGson; }

    public void logout(){
        ua = null;
        db.userAuthorizationInfoDao().deleteUserAuthorizationInfo();
    }

    public String retrieveAuthorizationURL() throws OAuthCommunicationException,
            OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException {
        return provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
    }

    public void setUserAuthorizationInfo(String pin, Context appContext)
            throws OAuthNotAuthorizedException, OAuthExpectationFailedException,
            OAuthCommunicationException, OAuthMessageSignerException {
        setAccessTokens(pin);
        setTwitterService();
        setUserInfo(appContext);
    }

    private void setAccessTokens(String pin) throws OAuthCommunicationException,
            OAuthExpectationFailedException, OAuthNotAuthorizedException, OAuthMessageSignerException {
        provider.retrieveAccessToken(consumer, pin);
        ua = new UserAndTokens();
        ua.setAccessToken(consumer.getToken());
        ua.setAccessTokenSecret(consumer.getTokenSecret());
    }

    public void setTwitterService(){
        consumer.setTokenWithSecret(ua.getAccessToken(), ua.getAccessTokenSecret());

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new SigningInterceptor(consumer))
                .build();

        Retrofit retrofitGson = new Retrofit.Builder()
                .baseUrl(Constants.TWITTER_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        twitterServiceGson = retrofitGson.create(TwitterService.class);
    }

    // context needed for getting filesDir where user profile image is stored
    private void setUserInfo(final Context appContext) {
        Call<User> getUserInfoCall = twitterServiceGson.getUserInfo();
        try {
            Response<User> response = getUserInfoCall.execute();
            if (response.isSuccessful()) {
                User user = response.body();
                ua.setUserId(user.getUserId());
                ua.setScreenName(user.getScreenName());
                ua.setProfileImageURLHttps(user.getProfileImageURLHttps());
                try (BufferedInputStream in = new BufferedInputStream(
                        new URL(ua.getProfileImageURLHttps()).openStream());
                     BufferedOutputStream out = new BufferedOutputStream(
                             appContext.openFileOutput(Constants.PROFILE_PIC_LOCAL_FILE_NAME,
                                     Context.MODE_PRIVATE))) {
                    Log.d("debug", appContext.getFilesDir().getAbsolutePath());
                    byte[] b = new byte[4 * 1024];
                    while (in.read(b) != -1) {
                        out.write(b);
                    }
                } catch (IOException e) {
                    Log.d("debug", "IOException occurred while downloading/persisting user profile image");
                }
            }
        } catch (IOException e) {
            Log.d("debug.network", "IOException occurred while talking to API server");
        }
    }

    public void persistUserAuthorizationInfo(){
        // because of OnConflict.REPLACE conflict strategy, this will update the sole record in the
        // user_authorization_info table if user_id matches
        db.userAuthorizationInfoDao().insertUserAuthorizationInfo(ua);
    }

    private int backUpToLastNonLetterCodePoint(String string, int startIndex, int endIndex){
        int originalEndIndex = endIndex;
        char ch = string.charAt(endIndex);
        if (Character.isHighSurrogate(ch)) {
            endIndex--;
        } else if (Character.isLetter(ch) && Character.isLetter(string.charAt(endIndex + 1))) {
            endIndex--;
            while (Character.isLetter(string.charAt(endIndex))) {
                endIndex--;
                // if for some reason a word is over (280 - headerLength) long, this method returns
                // the original value of the endIndex
                if (endIndex == startIndex){
                    return originalEndIndex;
                }
            }
        }
        return endIndex;
    }

    public void tweetDraft(Draft draft) throws IOException, RuntimeException, TwitterApiException {
        final String titleTweet;
        final String bodyTweet = draft.getBody();
        if (draft.getTitle().isEmpty()){
            titleTweet = "Thread:";
        } else {
            titleTweet = draft.getTitle() + ": A Thread";
        }

        // tweets 1st tweet in thread (thread title tweet)
        Response<StatusId> res = twitterServiceGson.postTweet(titleTweet).execute();
        if (!res.isSuccessful()){
            // throws TwitterApiException if server communication is fine but an API specific
            // error occurs, which contains the twitter API error message
            Gson gson = new Gson();
            TwitterErrors twitterErrors = gson.fromJson(res.errorBody().string(), TwitterErrors.class);
            StringBuilder builder = new StringBuilder();
            for (TwitterErrors.Error error : twitterErrors.getErrors()){
                builder.append(error.getTwitterErrorMessage());
                builder.append("\n");
            }
            throw (new TwitterApiException(builder.toString()));
        }

        String previousStatusId = res.body().getStatusId();
        Log.d("debug.twitterapi", "previousStatusId = " + previousStatusId);
        final String tag = "@" + ua.getScreenName();
        final int tagLength = tag.length();

        // Both begin index and end index are inclusive.
        int startIndex = 0;
        int endIndex = 0;
        int tweetCount = 1;
        int headerLength = tagLength + Integer.toString(tweetCount).length() + 3;

        while (startIndex < bodyTweet.length()) {
            endIndex = startIndex + Constants.TWEET_MAX_CHAR - headerLength;
            if (endIndex >= bodyTweet.length()) {
                endIndex = bodyTweet.length() - 1;
            } else {
                endIndex = backUpToLastNonLetterCodePoint(bodyTweet, startIndex, endIndex);
            }

            TwitterTextParseResults parseResult = TwitterTextParser.parseTweet(
                        bodyTweet.substring(startIndex, endIndex + 1));

            while (parseResult.weightedLength > (Constants.TWEET_MAX_CHAR - headerLength)){
                endIndex -= (endIndex - (140 - headerLength)) / 2;
                endIndex = backUpToLastNonLetterCodePoint(bodyTweet, startIndex, endIndex);
                parseResult = TwitterTextParser.parseTweet(
                        bodyTweet.substring(startIndex, endIndex + 1));
            }

            Map<String, String> map = new HashMap<String, String>();
            map.put("status", tweetCount + "/ " + tag + "\n" +
                    bodyTweet.substring(startIndex, endIndex + 1));
            map.put("in_reply_to_status_id", previousStatusId);

            res = twitterServiceGson.replyToTweet(map).execute();
            if (!res.isSuccessful()){
                Gson gson = new Gson();
                TwitterErrors twitterErrors = gson.fromJson(res.errorBody().string(), TwitterErrors.class);
                StringBuilder builder = new StringBuilder();
                for (TwitterErrors.Error error : twitterErrors.getErrors()){
                    builder.append(error.getTwitterErrorMessage());
                    builder.append("\n");
                }
                throw (new TwitterApiException(builder.toString()));
            }

            previousStatusId = res.body().getStatusId();
            startIndex = endIndex + 1;
            tweetCount ++;
        }
    }

    public static boolean hasNetworkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) return false;
            // accepts cellular, wifi and ethernet connection
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                return true;
            } else {
                return false;
            }
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }
}
