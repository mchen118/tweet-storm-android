package com.muchen.tweetstormmaker.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.muchen.tweetstormmaker.R;
import com.muchen.tweetstormmaker.constants.Constants;
import com.muchen.tweetstormmaker.database.AppDatabase;
import com.muchen.tweetstormmaker.databinding.ActivityDraftEditBinding;
import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.presenters.DraftPresenter;
import com.muchen.tweetstormmaker.presenters.DraftPresenterInterface;

import java.util.List;

public class DraftEditActivity extends BaseActivity implements DraftViewInterface {
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
        // sets up draft presenter
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
            showToast("Cannot Tweet with Empty Body", Toast.LENGTH_LONG);
            return;
        }

        boolean tweetSuccessful = twitterApiPresenter.tweetDraft(new Draft(title, body));
        if (tweetSuccessful) {
            draftPresenter.deleteDraftById(draftId);
            showToast("Tweet Successful", Toast.LENGTH_LONG);
            startActivity(new Intent(this, DraftListActivity.class));
        }
    }

    public void onSaveButtonClick(View v) {
        // getText().toString() does not return null
        final String title = binding.titleEditText.getText().toString();
        final String body = binding.bodyEditText.getText().toString();
        // in case draft title or body is empty
        if (title.isEmpty() && body.isEmpty()){
            showToast("Cannot Save Empty Draft", Toast.LENGTH_LONG);
            return;
        }
        // in case draft content is not empty, and a new draft is being edited upon
        if (draftId == -1){
            draftId = draftPresenter.insertNewDraftAndReturnId(new Draft(title, body));
            if (draftId == -1) {
                showToast("Draft Failed to Save", Toast.LENGTH_LONG);
                return;
            }
            showToast("Draft Saved", Toast.LENGTH_LONG);
            return;
        }
        // In case draft content is not empty, and an existing draft is being edited upon (update),
        // or in case the draft has been tweeted out but for some reason the user still wants to
        // save it (insert).
        draftPresenter.updateDraft(new Draft(draftId, title, body));
        showToast("Draft Saved", Toast.LENGTH_LONG);
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
