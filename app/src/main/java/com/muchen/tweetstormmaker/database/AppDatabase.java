package com.muchen.tweetstormmaker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.muchen.tweetstormmaker.models.Draft;
import com.muchen.tweetstormmaker.models.UserAndTokens;

@Database(entities = {Draft.class, UserAndTokens.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final Object lock = new Object();
    private static final String DATABASE_NAME = "drafts";
    private static volatile AppDatabase soleInstance;

    public static AppDatabase soleInstance(Context context){
        if(soleInstance == null){
            synchronized (lock){
                if(soleInstance == null){
                    soleInstance = Room.databaseBuilder(context, AppDatabase.class, AppDatabase.DATABASE_NAME)
                            .build();
                }
            }
        }
        return soleInstance;
    }

    public abstract DraftDao draftDao();

    public abstract UserAndTokensDao userAuthorizationInfoDao();
}
