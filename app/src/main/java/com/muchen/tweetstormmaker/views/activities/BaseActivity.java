package com.muchen.tweetstormmaker.views.activities;

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

import com.muchen.tweetstormmaker.R;
import com.muchen.tweetstormmaker.concurrent.AppExecutorServices;
import com.muchen.tweetstormmaker.constants.Constants;
import com.muchen.tweetstormmaker.database.AppDatabase;
import com.muchen.tweetstormmaker.models.UserAndTokens;
import com.muchen.tweetstormmaker.presenters.TwitterApiPresenter;
import com.muchen.tweetstormmaker.presenters.TwitterApiPresenterInterface;
import com.muchen.tweetstormmaker.restservice.TwitterApi;
import com.muchen.tweetstormmaker.views.actionproviders.UserInfoActionProvider;
import com.muchen.tweetstormmaker.views.fragments.PinDialogFragment;
import com.muchen.tweetstormmaker.views.fragments.RedirectDialogFragment;

@SuppressLint("Registered")
// BaseActivity implements app bar, and dialog fragments
public class BaseActivity extends AppCompatActivity implements
        RedirectDialogFragment.RedirectDialogListenerInterface,
        PinDialogFragment.PinDialogListenerInterface,
        TwitterApiViewInterface {
    protected boolean redirectAccepted = false;
    protected Menu optionsMenu = null;

    protected TwitterApi twitterApi;
    protected AppExecutorServices executorServices;
    protected TwitterApiPresenterInterface twitterApiPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // gets AppExecutorServices singleton instance
        executorServices = AppExecutorServices.soleInstance();
        // gets TwitterApi singleton instance and initializes it
        twitterApi = TwitterApi.soleInstance(
                getResources().getString(R.string.CONSUMER_KEY),
                getResources().getString(R.string.CONSUMER_KEY_SECRET),
                AppDatabase.soleInstance(getApplicationContext()));
        twitterApiPresenter = new TwitterApiPresenter(this, twitterApi, executorServices);
    }

    @Override
    protected void onStart(){
        super.onStart();
        // null check to ensure optionsMenu has been created, which is completed in the
        // onCreateOptionsMenu call, the time of whose calling is after onResume()
        if (optionsMenu != null){ twitterApiPresenter.getOrFetchUserInfo(); }

        if (redirectAccepted){
            redirectAccepted = false;
            showPinDialogFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        twitterApiPresenter.getOrFetchUserInfo();
        return true;
    }

    // overrides method to set up the app bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
            case R.id.action_user_info:
                return true;
            case R.id.action_login:
                if (!TwitterApi.hasNetworkConnection(this)){
                    Toast.makeText(this,"No Internet Connection Available",Toast.LENGTH_LONG).show();
                } else {
                    twitterApiPresenter.redirectToAuthorization();
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
        // because of the executor service used in the below two calls is a single thread executor,
        // it can be assured that these two tasks below will be executed sequentially
        // see https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html#newSingleThreadExecutor--
        twitterApiPresenter.setAndPersistUserAndTokens(pin, getApplicationContext());
        twitterApiPresenter.getOrFetchUserInfo();
    }

    // twitter api view interface implementation
    @Override
    public void adjustOptionsMenuItemVisibility(UserAndTokens u){
        Log.d("debug", "adjustOptionsMenuItemVisibility called");
        MenuItem searchMenuItem = optionsMenu.findItem(R.id.action_search);
        MenuItem loginMenuItem = optionsMenu.findItem(R.id.action_login);
        MenuItem userInfoMenuItem = optionsMenu.findItem(R.id.action_user_info);
        if (u != null) {
            runOnUiThread(()->{
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

                UserInfoActionProvider actionProvider = new UserInfoActionProvider(
                        this,
                        u.getScreenName(),
                        twitterApi,
                        userInfoMenuItem);
                MenuItemCompat.setActionProvider(userInfoMenuItem, actionProvider);
            });
        } else {
            runOnUiThread(()->{
                userInfoMenuItem.setVisible(false);
                loginMenuItem.setVisible(true);
                Log.d("debug.actionbar", "userInfoMenuItem visibility off");
            });
        }
    }

    @Override
    public void showToast(String msg, int duration) {
        Toast toast = Toast.makeText(this, msg, duration);
        runOnUiThread(()-> toast.show());
    }

    @Override
    public void showRedirectDialogFragment(String redirectUrl) {
        runOnUiThread(()->{
            RedirectDialogFragment frag = RedirectDialogFragment.newInstance(redirectUrl);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(0, frag, null);
            fragmentTransaction.commit();
        });
    }

    public void showPinDialogFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(0, new PinDialogFragment(), null);
        fragmentTransaction.commit();
    }
}
