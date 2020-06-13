package com.muchen.tweetstormandroid.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.muchen.tweetstormandroid.models.Draft;

import java.util.List;

@Dao
public interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertDrafts(Draft... drafts);

    // If no primary key matches, this method doesn't change the DB.
    @Update
    void updateDrafts(Draft... drafts);

    @Delete
    void deleteDrafts(Draft... drafts);

    @Query("DELETE FROM drafts")
    void nukeTable();

    @Query("SELECT * FROM drafts WHERE draft_id == :id")
    Draft fetchDraftById(long id);

    // This returns the first result obtained by the cursor, which is an assertion from
    // examining DraftDao.Impl.java. If no the cursor obtains no result, null is returned.
    @Query("SELECT * FROM drafts WHERE last_modified == :lastModified")
    Draft fetchDraftByLastModified(long lastModified);

    // This method does not return null - an empty list is returned in case result set is empty.
    @Query("SELECT * FROM drafts WHERE body like :pattern OR title like :pattern")
    List<Draft> fetchDraftsByLikeness(String pattern);

    @Query("SELECT * FROM drafts")
    List<Draft> fetchAllDrafts();
}
