package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by neeraj on 2/5/16.
 */
public class MoviesOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "moviedb";
    private static final String MOVIES_TABLE_NAME = "movies";
    private static final String KEY_ID = "_id";
    private static final String KEY_MOVIE_TITLE= "title";
    private static final String KEY_MOVIE_RELEASE_DATE ="releasedate";
    private static final String KEY_MOVIE_POSTER = "poster";
    private static final String KEY_MOVIE_USER_RATING = "rating";
    private static final String KEY_MOVIE_OVERVIEW = "overview";
    private static final String KEY_MOVIE_BACKDROP = "backdrop";
    private static final String MOVIES_TABLE_CREATE =
            "CREATE TABLE " + MOVIES_TABLE_NAME + " (" +
                    KEY_ID + " TEXT, " +
                    KEY_MOVIE_TITLE + " TEXT, " +
                    KEY_MOVIE_POSTER + " TEXT, " +
                    KEY_MOVIE_RELEASE_DATE + " TEXT, " +
                    KEY_MOVIE_USER_RATING + " TEXT, "+
                    KEY_MOVIE_BACKDROP + " TEXT, "+
                    KEY_MOVIE_OVERVIEW + " TEXT);";

    MoviesOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MOVIES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
