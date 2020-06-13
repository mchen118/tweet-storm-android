package com.muchen.tweetstormandroid.views;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.muchen.tweetstormandroid.R;
import com.muchen.tweetstormandroid.concurrent.AppExecutorServices;
import com.muchen.tweetstormandroid.constants.Constants;
import com.muchen.tweetstormandroid.database.AppDatabase;
import com.muchen.tweetstormandroid.models.UserAuthorizationInfo;
import com.muchen.tweetstormandroid.restservice.TwitterApi;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

@SuppressLint("Registered")
// BaseActivity implements app bar, and dialog fragments
public class BaseActivity extends AppCompatActivity implements
        RedirectDialogFragment.RedirectDialogListenerInterface,
        PinDialogFragment.PinDialogListenerInterface{
    protected boolean redirectAccepted = false;
    protected Menu optionsMenu = null;

    protected AppExecutorServices executorServices;
    protected TwitterApi twitterApi;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // gets AppExecutorServices singleton instance
        executorServices = AppExecutorServices.soleInstance();
        // gets TwitterApi singleton instance and initializes it
        twitterApi = TwitterApi.soleInstance(getResources().getString(R.string.CONSUMER_KEY),
                getResources().getString(R.string.CONSUMER_KEY_SECRET),
                AppDatabase.soleInstance(getApplicationContext()));
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (optionsMenu != null){
            if (twitterApi.getUserAuthorizationInfo() != null){
                // this avoids querying database if UserAuthorizationInfo exists in memory (i.e.
                // in the twitterApi instance)
                Log.d("debug.actionbar", "twitterApi.ua != null");
                runOnUiThread(()-> adjustOptionsMenuItemVisibility(
                        twitterApi.getUserAuthorizationInfo()));
            } else {
                Log.d("debug.actionbar", "twitterApi.ua == null");
                executorServices.diskIO().execute(()->{
                    UserAuthorizationInfo ua = twitterApi.fetchUserAuthorizationInfo();
                    runOnUiThread(()-> adjustOptionsMenuItemVisibility(ua));
                });
            }
        }
        if (redirectAccepted){
            redirectAccepted = false;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(0, new PinDialogFragment(), null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        if (twitterApi.getUserAuthorizationInfo() != null){
            // this avoids querying database if UserAuthorizationInfo exists in memory (i.e.
            // in the twitterApi instance)
            Log.d("debug.actionbar", "twitterApi.ua != null");
            runOnUiThread(()-> adjustOptionsMenuItemVisibility(
                    twitterApi.getUserAuthorizationInfo()));
        } else {
            Log.d("debug.actionbar", "twitterApi.ua == null");
            executorServices.diskIO().execute(()->{
                UserAuthorizationInfo ua = twitterApi.fetchUserAuthorizationInfo();
                runOnUiThread(()-> adjustOptionsMenuItemVisibility(ua));
            });
        }
        return true;
    }

    private void adjustOptionsMenuItemVisibility(UserAuthorizationInfo ua){
        MenuItem searchMenuItem = optionsMenu.findItem(R.id.action_search);
        MenuItem loginMenuItem = optionsMenu.findItem(R.id.action_login);
        MenuItem userInfoMenuItem = optionsMenu.findItem(R.id.action_user_info);
        if (ua != null) {
            userInfoMenuItem.setVisible(true);
            loginMenuItem.setVisible(false);
            Log.d("debug.actionbar", "userInfoMenuItem visibility on");
            Drawable userProfileImage = Drawable.createFromPath((getFileStreamPath(
                    Constants.PROFILE_PIC_LOCAL_FILE_NAME).getPath()));
            userInfoMenuItem.setIcon(userProfileImage);
            userInfoMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                public boolean onMenuItemActionExpand(MenuItem item) {
                    searchMenuItem.setVisible(false);
                    // return true to allow expansion, false to suppress
                    return true;
                }

                public boolean onMenuItemActionCollapse(MenuItem item) {
                    invalidateOptionsMenu();
                    // return true to allow collapse, false to suppress
                    return true;
                }
            });

            UserInfoActionProvider actionProvider = new UserInfoActionProvider(this,
                    ua.getScreenName(), twitterApi, userInfoMenuItem);
            MenuItemCompat.setActionProvider(userInfoMenuItem, actionProvider);

        } else {
            userInfoMenuItem.setVisible(false);
            loginMenuItem.setVisible(true);
            Log.d("debug.actionbar", "userInfoMenuItem visibility off");
        }
    }

    // overrides method to set up the app bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_user_info:
                return true;
            case R.id.action_login:
                if (!TwitterApi.hasNetworkConnection(this)){
                    Toast.makeText(this,"No Internet Connection Available",Toast.LENGTH_LONG).show();
                } else {
                    executorServices.networkIO().execute(()->{
                        try {
                            String redirectUrl = twitterApi.retrieveAuthorizationURL();
                            runOnUiThread(()->{
                                RedirectDialogFragment frag = RedirectDialogFragment.newInstance(redirectUrl);
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.add(0, frag, null);
                                fragmentTransaction.commit();
                            });
                        } catch (OAuthMessageSignerException |
                                OAuthNotAuthorizedException |
                                OAuthExpectationFailedException |
                                OAuthCommunicationException e){
                            Log.d("debug.signpost", e.toString());
                            Toast.makeText(this,"Login Failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment frag){
        if (frag instanceof RedirectDialogFragment){
            ((RedirectDialogFragment) frag).setOnRedirectDialogListener(this);
        } else if (frag instanceof PinDialogFragment){
            ((PinDialogFragment) frag).setOnPinDialogListener(this);
        }
    }

    // redirect dialog listener interface implementation
    @Override
    public void onRedirectPositiveButtonClick() { redirectAccepted = true; }

    // pin dialog listener interface implementation
    @Override
    public void onPinPositiveButtonClick(String pin) {
        executorServices.networkIO().execute(()->{
            try{
                twitterApi.setUserAuthorizationInfo(pin, getApplicationContext());
                twitterApi.persistUserAuthorizationInfo();
            } catch (OAuthMessageSignerException |
                    OAuthNotAuthorizedException |
                    OAuthExpectationFailedException |
                    OAuthCommunicationException e){
                Log.d("debug.signpost", e.toString());
            }
            runOnUiThread(()-> adjustOptionsMenuItemVisibility(
                    twitterApi.getUserAuthorizationInfo()));
        });
    }
}
