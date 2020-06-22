package com.muchen.tweetstormmaker.presenters;

import android.util.Log;

import com.muchen.tweetstormmaker.concurrent.AppExecutorServices;
import com.muchen.tweetstormmaker.database.AppDatabase;
import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.views.activities.DraftViewInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DraftPresenter implements DraftPresenterInterface {
    private DraftViewInterface view;
    private AppDatabase db;
    private AppExecutorServices executorServices;

    public DraftPresenter(DraftViewInterface view, AppDatabase db, AppExecutorServices executorServices){
        this.db = db;
        this.view = view;
        this.executorServices = executorServices;
    }

    @Override
    public void fetchAllDrafts() {
        executorServices.diskIO().execute(()->{
            List<Draft> allDrafts = db.draftDao().fetchAllDrafts();
            view.displayDrafts(allDrafts);
        });
    }

    @Override
    public void fetchDraftById(long draftId) {
        executorServices.diskIO().execute(()->{
            List<Draft> draftList = new ArrayList<Draft>();
            draftList.add(db.draftDao().fetchDraftById(draftId));
            view.displayDrafts(draftList);
        });
    }

    @Override
    public long insertNewDraftAndReturnId(Draft draft) {
        Future<Long> future = executorServices.diskIO().submit(() -> {
            List<Long> draftIdList = db.draftDao().insertDrafts(draft);
            return draftIdList.get(0);
        });

        try {
            long result = future.get(2, TimeUnit.SECONDS);
            return result;
        } catch (ExecutionException | InterruptedException | TimeoutException e){
            Log.d("debug.database", e.toString());
            return -1;
        }
    }

    @Override
    public void updateDraft(Draft draft) {
        executorServices.diskIO().execute(()-> db.draftDao().updateDrafts(draft));
    }

    @Override
    public void deleteDraftById(long draftId) {
        executorServices.diskIO().execute(()->{
            // deleteDrafts() only matches primary key to delete
            db.draftDao().deleteDrafts(new Draft(draftId, null, null));
        });
    }

    @Override
    public void handleDraftQuery(String pattern) {
        executorServices.diskIO().execute(()-> {
            List<Draft> selectedDrafts = db.draftDao().fetchDraftsByLikeness(pattern);
            view.displayDrafts(selectedDrafts);
        });
    }
}
