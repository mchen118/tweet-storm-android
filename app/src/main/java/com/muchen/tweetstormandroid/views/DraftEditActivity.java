package com.muchen.tweetstormandroid.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.muchen.tweetstormandroid.R;
import com.muchen.tweetstormandroid.constants.Constants;
import com.muchen.tweetstormandroid.database.AppDatabase;
import com.muchen.tweetstormandroid.databinding.ActivityDraftEditBinding;

import com.muchen.tweetstormandroid.models.Draft;
import com.muchen.tweetstormandroid.presenters.DraftPresenter;
import com.muchen.tweetstormandroid.presenters.DraftPresenterInterface;
import com.muchen.tweetstormandroid.restservice.TwitterApiException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DraftEditActivity extends BaseActivity implements DraftInterface{
    private long draftId = -1;
    private ActivityDraftEditBinding binding;
    private DraftPresenterInterface draftPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Log.d("debug.lifecycle", "DraftEditActivity onStart()");
        super.onCreate(savedInstanceState);
        // sets up view binding
        binding = ActivityDraftEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // sets app bar
        setSupportActionBar(binding.activityDraftEditToolBar);
        // sets up presenter
        draftPresenter = new DraftPresenter( this, AppDatabase.soleInstance(getApplicationContext()),
                executorServices);
        // fetches existing draft if needed
        if (getIntent().hasExtra(Constants.EXTRA_DRAFT_ID)){
            draftId = getIntent().getLongExtra(Constants.EXTRA_DRAFT_ID, -1);
            draftPresenter.fetchDraftById(draftId);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debug.lifecycle", "DraftEditActivity onStart()");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("debug.lifecycle", "DraftEditActivity onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug.lifecycle", "DraftEditActivity onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("debug.lifecycle", "DraftEditActivity onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debug.Lifecycle", "DraftEditActivity onDestroy()");
    }

    // overridden method that sets up the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d("debug.lifecycle", "DraftEditActivity onCreateOptionsMenu()");
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setVisible(false);
        searchMenuItem.setEnabled(false);
        return true;
    }

    public void onTweetButtonClick(View v){
        // getText().toString() does not return null
        final String title = binding.titleEditText.getText().toString();
        final String body = binding.bodyEditText.getText().toString();
        // in case draft body is empty
        if (body.isEmpty()){
            Toast.makeText(this, "Cannot Tweet with Empty Body", Toast.LENGTH_LONG).show();
            return;
        }

        executorServices.diskIO().execute(()->{
            twitterApi.fetchUserAuthorizationInfo();
            if (twitterApi.getUserAuthorizationInfo() != null){
                // in case user has logged in
                if (twitterApi.getTwitterService() == null) { twitterApi.setTwitterService(); }

                executorServices.networkIO().execute(()->{
                    try {
                        twitterApi.tweetDraft(new Draft(title, body));
                    } catch (IOException e) {
                        Log.d("debug.network", e.toString());
                        runOnUiThread(()->
                            Toast.makeText(this, "Encountered a network error while tweeting - " +
                                            "a portion of your draft could have been tweeted out already.",
                                    Toast.LENGTH_LONG).show());
                        return;
                    } catch (RuntimeException e){
                        runOnUiThread(()->
                                Toast.makeText(this, "Encountered an unexpected error while tweeting - " +
                                            "a portion of your draft could have been tweeted out already.",
                                    Toast.LENGTH_LONG).show());
                        Log.d("debug.twitterapi", e.toString());
                        return;
                    } catch (TwitterApiException e){
                        runOnUiThread(()->
                                Toast.makeText(this, "Encountered a Twitter API error while tweeting - " +
                                            "a portion of your draft could have been tweeted out already.",
                                    Toast.LENGTH_LONG).show());
                        Log.d("debug.twitterapi", e.toString());
                        return;
                    }

                    draftPresenter.deleteDraftById(draftId);
                    runOnUiThread(()->{
                        Toast.makeText(this, "Tweet Successful.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(this, DraftViewActivity.class));
                    });
                });
            } else {
                // in case user has not logged in/completed authorization
                runOnUiThread(()->
                        Toast.makeText(this, "Please Login First", Toast.LENGTH_LONG).show());
            }
        });
    }

    public void onSaveButtonClick(View v) {
        // getText().toString() does not return null
        final String title = binding.titleEditText.getText().toString();
        final String body = binding.bodyEditText.getText().toString();
        // in case draft title or body is empty
        if (title.isEmpty() && body.isEmpty()){
            Toast.makeText(this, "Cannot Save Empty Draft", Toast.LENGTH_LONG).show();
            return;
        }
        // in case draft content is not empty, and a new draft is being edited upon
        if (draftId == -1){
            try {
                draftId = draftPresenter.insertNewDraftAndReturnId(new Draft(title, body));
            } catch (ExecutionException | InterruptedException e) {
                Log.d("debug.database", e.toString());
                Toast.makeText(this, "Draft Failed To Save", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, "Draft Saved", Toast.LENGTH_LONG).show();
            return;
        }
        // In case draft content is not empty, and an existing draft is being edited upon (update),
        // or in case the draft has been tweeted out but for some reason the user still wants to
        // save it (insert).
        draftPresenter.updateDraft(new Draft(draftId, title, body));
        Toast.makeText(this, "Draft Saved", Toast.LENGTH_LONG).show();
    }

    // draft interface implementation
    @Override
    public void displayDrafts(List<Draft> drafts) {
        runOnUiThread(()->{
            binding.titleEditText.setText(drafts.get(0).getTitle());
            binding.bodyEditText.setText(drafts.get(0).getBody());
        });
    }
}
