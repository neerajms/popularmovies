package com.neerajms99b.neeraj.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MovieDetailsActivity extends AppCompatActivity {

    private String mMovieId;
    private String mMovieTitle;
    private String mMoviePosterPath;
    private String mMovieUserRating;
    private String mMovieReleaseDate;
    private String mMoviePlot;
    private String mMovieBackDropPath;
    private static String posterPath;
    private static String backDropPath;
    private boolean dataSetChanged;
    private ArrayList<String> mTrailersList;
    String mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/";
    final String mApiKeyParam = "api_key";
    final String mKeyValue = "2b34f0a753ed8e38b7546773dbed2720";
    String mTrailerKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mTrailerKey = intent.getExtras().getString("trailer");
        mMovieId = intent.getExtras().getString("movieId");
        mMovieTitle = intent.getExtras().getString("movieTitle");
        mMoviePosterPath = intent.getExtras().getString("moviePosterFullPath");
        mMovieUserRating = intent.getExtras().getString("movieUserRating");
        mMovieReleaseDate = intent.getExtras().getString("movieReleaseDate");
        mMoviePlot = intent.getExtras().getString("moviePlot");
        mMovieBackDropPath = intent.getExtras().getString("movieBackDropPath");
        mTrailersList = new ArrayList<String>();
//        fetchTrailers();
        ImageView imageView = (ImageView) findViewById(R.id.back_drop_image);
        if (MainActivityFragment.noNetwork) {
            FileInputStream inputStream;
            try {
                File f = new File(mMovieBackDropPath, mMovieId + "back.png");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//                ImageView img=(ImageView)findViewById(R.id.temp_image);
//                img.setImageBitmap(b);
                imageView.setImageBitmap(b);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {


            Picasso.with(this).load(mMovieBackDropPath).placeholder(R.drawable.placeholder_loading).into(imageView);
        }

//        BitmapDrawable backDropBitmapDrawable = (BitmapDrawable)imageView.getDrawable();
//        Bitmap bitmapBackDrop = backDropBitmapDrawable.getBitmap();
//
//        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
//        bitmapBackDrop.compress(Bitmap.CompressFormat.PNG,100, baos);
//        byte [] b=baos.toByteArray();
//        String stringBackDrop= Base64.encodeToString(b, Base64.DEFAULT);
//        setTitle(mMovieTitle);

//        SharedPreferences sharedPref = this.getSharedPreferences(
//                "trailerShared", Context.MODE_PRIVATE);
//
//        String trailerKey = sharedPref.getString("trailerkey","trailerkey");

//        Log.d("TrailerKey MovDetAct::",trailerKey);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container,
                            MovieDetailsFragment.newInstance(
                                    mMovieId,
                                    mMovieTitle,
                                    mMoviePosterPath,
                                    mMovieUserRating,
                                    mMovieReleaseDate,
                                    mMoviePlot,
                                    mMovieBackDropPath), "MovieDetailsFragment").commit();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
//    public void fetchTrailers() {
//
//        String fetchTrailersUrl = mFetchMoviesBaseUrl + mMovieId + "/" + "videos";
//        Uri builtUri = Uri.parse(fetchTrailersUrl)
//                .buildUpon()
//                .appendQueryParameter(mApiKeyParam, mKeyValue)
//                .build();
//        String url = builtUri.toString();
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        FetchDataTask fetchMoviesTask = new FetchDataTask();
//        VolleyCallBack volleyCallBack = new VolleyCallBack() {
//            @Override
//            public void returnResponse(String result) {
//
//                getTrailersFromJson(result);
//
//            }
//        };
//        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
//    }
//
//    public void getTrailersFromJson(String jsonResult) {
//        String trailerKey;
//        final String TMDB_RESULTS = "results";
//        final String TMDB_TRAILER_KEY = "key";
//        try {
//            JSONObject trailersJsonObject = new JSONObject(jsonResult);
//            JSONArray trailersArray = trailersJsonObject.getJSONArray(TMDB_RESULTS);
//            mTrailersList.clear();
//            for (int i = 0; i < trailersArray.length(); i++) {
//                trailerKey = trailersArray.getJSONObject(i).getString(TMDB_TRAILER_KEY);
//                mTrailersList.add(trailerKey);
//
//            }
//
////            mTrailerAdapter.notifyDataSetChanged();
//
//        } catch (JSONException je) {
//
//        }
////        if(!mTrailersList.isEmpty()){
////            mShareUrl = "http://www.youtube.com/watch?v=" + mTrailersList.get(0);
////        }else {
////            mShareUrl = "Not found";
////        }
//
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//        if (shareActionProvider != null) {
//            shareActionProvider.setShareIntent(MovieDetailsFragment.createShareTrailerIntent());
//        } else {
//            Log.e("Error", "Intent not found");
//        }
//        return true;
////        return super.onPrepareOptionsMenu(menu);
//    }

    public void favoriteMovie(View view) {
        posterPath = getFilesDir().getAbsolutePath();
        backDropPath = getFilesDir().getAbsolutePath();
        ImageButton imageButton = (ImageButton) findViewById(R.id.favorite_button);

        if (imageButton.getTag().equals("R.drawable.favorite")) {
            imageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
            imageButton.setTag("R.drawable.favorite_clicked");
            Toast.makeText(this, "Movie favorited", Toast.LENGTH_SHORT).show();
            MainActivityFragment.mDataSetChanged = false;


            ImageView posterImageView = (ImageView) findViewById(R.id.movie_poster_image_view);
            BitmapDrawable posterBitmapDrawable = (BitmapDrawable) posterImageView.getDrawable();
            Bitmap bitmapPoster = posterBitmapDrawable.getBitmap();

            ImageView backDropImageView = (ImageView) findViewById(R.id.back_drop_image);
            BitmapDrawable backDropBitmapDrawable = (BitmapDrawable) backDropImageView.getDrawable();
            Bitmap bitmapBackDrop = backDropBitmapDrawable.getBitmap();

            saveImage(bitmapPoster, mMovieId);
            saveImage(bitmapBackDrop, mMovieId + "back");
//            FileInputStream inputStream;
//            try {
//                File f=new File(backDropPath, mMovieId+"back.png");
//                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//                ImageView img=(ImageView)findViewById(R.id.temp_image);
//                img.setImageBitmap(b);
//
//            }
//            catch (FileNotFoundException e)
//            {
//                e.printStackTrace();
//            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesContentProvider.KEY_ID, mMovieId);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_TITLE, mMovieTitle);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_BACKDROP, backDropPath);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_OVERVIEW, mMoviePlot);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_POSTER, posterPath);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_RELEASE_DATE, mMovieReleaseDate);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_USER_RATING, mMovieUserRating);
            Uri uri = getContentResolver().insert(MoviesContentProvider.uri, contentValues);
//            Log.d("saved",String.valueOf(uri));

        } else if (imageButton.getTag().equals("R.drawable.favorite_clicked")) {

            imageButton.setImageResource(R.drawable.ic_action_favourite);
            imageButton.setTag("R.drawable.favorite");
            Toast.makeText(this, "Movie unfavorited", Toast.LENGTH_SHORT).show();
            String url = String.valueOf(MoviesContentProvider.uri) + "/" + mMovieId;
            Uri queryUri = Uri.parse(url);
//            Log.d("url to delete:::::", String.valueOf(queryUri));
            int i = getContentResolver().delete(queryUri, null, null);
//            Log.d("Value of i", String.valueOf(i));
            if (i > 0) {
                MainActivityFragment.mDataSetChanged = true;
//                Log.d("Poster path::::::", posterPath);
                File posterFile = new File(posterPath, mMovieId + ".png");
                boolean deletedPoster = posterFile.delete();
                File backDropFile = new File(backDropPath, mMovieId + "back.png");
                boolean deletedBackDrop = backDropFile.delete();
//                if (deletedPoster) {
//                    Log.d("POSTER status:", "deleted");
//                }
//                if (deletedBackDrop) {
//                    Log.d("Backdrop status:", "deleted");
//                }
            }
        }

    }

    public void saveImage(Bitmap bitmap, String fileName) {
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        File myDir = cw.getDir("movies", Context.MODE_PRIVATE);
//        if (!myDir.exists()) {
//            myDir.mkdir();
//        }
        File image = new File(getFilesDir(), fileName + ".png");
        boolean success = false;

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {

            outStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        /* 100 to keep full quality of the image */

            outStream.flush();
            outStream.close();
            success = true;
//            Log.d("File Operation:", mMovieId + ".png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}