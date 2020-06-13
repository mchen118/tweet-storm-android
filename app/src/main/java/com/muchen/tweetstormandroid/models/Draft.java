package com.muchen.tweetstormandroid.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity(tableName = "drafts")
public class Draft {
    // "If the field type is long or int (or its TypeConverter converts it to a long or int),
    // Insert methods treat 0 as not-set while inserting the item."
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "draft_id")
    private long draftId;

    @ColumnInfo(name = "last_modified")
    private long lastModified;

    private String title;

    private String body;

    public Draft(){
        lastModified = Calendar.getInstance().getTimeInMillis();
        draftId = 0;
    }

    // Room prefers constructor with no parameter.
    @Ignore
    public Draft(String title, String body){
        draftId = 0;
        lastModified = Calendar.getInstance().getTimeInMillis();
        this.title = title;
        this.body = body;
    }

    @Ignore
    public Draft(long draftId, String title, String body){
        this.draftId = draftId;
        lastModified = Calendar.getInstance().getTimeInMillis();
        this.title = title;
        this.body = body;
    }

    public void setTitle(String title){ this.title = title; }

    public String getTitle(){ return title; }

    public void setBody(String body){ this.body = body; }

    public String getBody(){ return body; }

    public void setLastModified(long t) { this.lastModified = t; }

    public long getLastModified() { return lastModified; }

    public void setDraftId(long id) { this.draftId = id; }

    public long getDraftId() { return draftId; }
}
