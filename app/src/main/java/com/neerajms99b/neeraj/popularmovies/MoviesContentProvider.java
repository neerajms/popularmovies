package com.neerajms99b.neeraj.popularmovies;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;


/**
 * Created by neeraj on 2/5/16.
 */
public class MoviesContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.neerajms99b.neeraj.popularmovies";
    private static final String TABLE_NAME = "movies";
    private static final String URL = "content://" + AUTHORITY + "/" + TABLE_NAME;
    public static Uri uri = Uri.parse(URL);

    private SQLiteDatabase database;

    public static final String KEY_ID = "_id";
    public static final String KEY_MOVIE_TITLE = "title";
    public static final String KEY_MOVIE_RELEASE_DATE = "releasedate";
    public static final String KEY_MOVIE_POSTER = "poster";
    public static final String KEY_MOVIE_USER_RATING = "rating";
    public static final String KEY_MOVIE_OVERVIEW = "overview";
    public static final String KEY_MOVIE_BACKDROP = "backdrop";

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME, 1);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", 2);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        MoviesOpenHelper dbHelper = new MoviesOpenHelper(context);
        database = dbHelper.getWritableDatabase();
        return (database != null) ? true : false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        switch (mUriMatcher.match(uri)) {
            case 1:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_id ASC";
                break;
            case 2:
                selection = "_id=" + uri.getLastPathSegment();
                Log.d("SELECTION:", selection);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        Log.d("Query:", String.valueOf(c));
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        ContentValues cv = contentValues;
        long rowID = database.insert(TABLE_NAME, null, contentValues);
        if (rowID > 0) {
            String _uri = String.valueOf(uri) + "/" + String.valueOf(cv.get(KEY_ID));
            Uri tempUri = Uri.parse(_uri);
            getContext().getContentResolver().notifyChange(tempUri, null);
            return tempUri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (mUriMatcher.match(uri)) {
            case 1:
                count = database.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case 2:
                String id = uri.getPathSegments().get(1);
                count = database.delete(TABLE_NAME, "_id = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
