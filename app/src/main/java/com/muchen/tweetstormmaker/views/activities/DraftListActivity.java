package com.muchen.tweetstormmaker.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.muchen.tweetstormmaker.R;
import com.muchen.tweetstormmaker.database.AppDatabase;
import com.muchen.tweetstormmaker.databinding.ActivityDraftViewBinding;
import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.presenters.DraftPresenter;
import com.muchen.tweetstormmaker.presenters.DraftPresenterInterface;
import com.muchen.tweetstormmaker.views.adapters.DraftAdapter;

import java.util.List;

public class DraftListActivity extends BaseActivity implements DraftViewInterface {
    private ActivityDraftViewBinding binding;
    private DraftPresenterInterface draftPresenter;
    private DraftAdapter draftAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("debug.lifecycle", "DraftListActivity onCreate()");
        // sets up view binding
        binding = ActivityDraftViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // sets app bar
        setSupportActionBar(binding.activityDraftViewToolBar);
        // sets up recycler view
        LinearLayoutManager manager = new LinearLayoutManager(this);
        draftAdapter = new DraftAdapter(getApplicationContext());
        binding.recyclerView.setLayoutManager(manager);
        binding.recyclerView.setAdapter(draftAdapter);
        // sets up presenter
        draftPresenter = new DraftPresenter( this, AppDatabase.soleInstance(getApplicationContext()),
                executorServices);
        // adds ItemTouchHelper to recycler view to support left swipes
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                draftPresenter.deleteDraftById(draftAdapter.getDraft
                        (viewHolder.getAdapterPosition()).getDraftId());
                draftPresenter.fetchAllDrafts();
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debug.lifecycle", "DraftListActivity onStart()");
        // updates UI based on data fetched from database
        draftPresenter.fetchAllDrafts();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d("debug.lifecycle", "DraftListActivity onResume()");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug.lifecycle", "DraftListActivity onPause()");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("debug.lifecycle", "DraftListActivity onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debug.lifecycle", "DraftListActivity onDestroy()");
    }

    // overridden method that sets up the app bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d("debug.lifecycle", "DraftListActivity onCreateOptionsMenu()");
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItem loginMenuItem = optionsMenu.findItem(R.id.action_login);
        MenuItem userInfoMenuItem = menu.findItem(R.id.action_user_info);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            public boolean onMenuItemActionExpand(MenuItem item) {
                userInfoMenuItem.setVisible(false);
                loginMenuItem.setVisible(false);
                // return true to allow expansion, false to suppress
                return true;
            }

            public boolean onMenuItemActionCollapse(MenuItem item) {
                invalidateOptionsMenu();
                draftPresenter.fetchAllDrafts();
                // return true to allow collapse, false to suppress
                return true;
            }
        });

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(final String s) {
                draftPresenter.handleDraftQuery("%" + s + "%");
                // return true to indicate query text submission has been handled
                return true;
            }

            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) draftPresenter.fetchAllDrafts();
                // return true to indicate text change has been handled
                return true;
            }
        });
        return true;
    }

    public void onFloatingActionButtonClick(View v){
        Intent intent = new Intent(DraftListActivity.this, DraftEditActivity.class);
        startActivity(intent);
    }

    // draft interface implementation
    @Override
    public void displayDrafts(List<Draft> drafts) {
        draftAdapter.setDrafts(drafts);
        runOnUiThread(()-> draftAdapter.notifyDataSetChanged());
    }
}
