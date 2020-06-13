package com.muchen.tweetstormandroid.presenters;

import com.muchen.tweetstormandroid.concurrent.AppExecutorServices;
import com.muchen.tweetstormandroid.database.AppDatabase;
import com.muchen.tweetstormandroid.models.Draft;
import com.muchen.tweetstormandroid.views.DraftInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DraftPresenter implements DraftPresenterInterface {
    private AppExecutorServices executorServices;
    private AppDatabase db;
    private DraftInterface view;

    public DraftPresenter(DraftInterface view, AppDatabase db, AppExecutorServices executorServices){
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
    public long insertNewDraftAndReturnId(Draft draft) throws ExecutionException, InterruptedException {
        Future<Long> future = executorServices.diskIO().submit(new Callable<Long>(){
            @Override
            public Long call() {
                List<Long> draftIdList = db.draftDao().insertDrafts(draft);
                return draftIdList.get(0);
            }
        });
        return future.get();
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
