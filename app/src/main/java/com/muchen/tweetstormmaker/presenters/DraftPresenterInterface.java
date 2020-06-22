package com.muchen.tweetstormmaker.presenters;

import com.muchen.tweetstormmaker.models.Draft;

public interface DraftPresenterInterface {
    void fetchAllDrafts();
    void fetchDraftById(long draftId);

    long insertNewDraftAndReturnId(Draft draft);

    void updateDraft(Draft draft);

    void deleteDraftById(long draftId);

    void handleDraftQuery(String pattern);
}
