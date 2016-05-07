package com.neerajms99b.neeraj.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mMovieId = intent.getExtras().getString("movieId");
        mMovieTitle = intent.getExtras().getString("movieTitle");
        mMoviePosterPath = intent.getExtras().getString("moviePosterFullPath");
        mMovieUserRating = intent.getExtras().getString("movieUserRating");
        mMovieReleaseDate = intent.getExtras().getString("movieReleaseDate");
        mMoviePlot = intent.getExtras().getString("moviePlot");
        mMovieBackDropPath = intent.getExtras().getString("movieBackDropPath");

        ImageView imageView = (ImageView) findViewById(R.id.back_drop_image);
        if (MainActivityFragment.offline) {
            try {
                File f = new File(mMovieBackDropPath, mMovieId + "back.png");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                imageView.setImageBitmap(b);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(this)
                    .load(mMovieBackDropPath)
                    .placeholder(R.drawable.placeholder_loading)
                    .into(imageView);
        }
        setTitle(mMovieTitle);

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

    public void favoriteMovie(View view) {
        posterPath = getFilesDir().getAbsolutePath();
        backDropPath = getFilesDir().getAbsolutePath();

        ImageButton imageButton = (ImageButton) findViewById(R.id.favorite_button);

        if (imageButton.getTag().equals("R.drawable.favorite")) {
            MainActivity.postersToBeDeleted.clear();
            
            imageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
            imageButton.setTag("R.drawable.favorite_clicked");
            Toast.makeText(this, "Movie added to Favorites", Toast.LENGTH_SHORT).show();

            MainActivityFragment.mDataSetChanged = false;

            ImageView posterImageView = (ImageView) findViewById(R.id.movie_poster_image_view);
            BitmapDrawable posterBitmapDrawable = (BitmapDrawable) posterImageView.getDrawable();
            Bitmap bitmapPoster = posterBitmapDrawable.getBitmap();

            ImageView backDropImageView = (ImageView) findViewById(R.id.back_drop_image);
            BitmapDrawable backDropBitmapDrawable = (BitmapDrawable) backDropImageView.getDrawable();
            Bitmap bitmapBackDrop = backDropBitmapDrawable.getBitmap();

            saveImage(bitmapPoster, mMovieId);
            saveImage(bitmapBackDrop, mMovieId + "back");

            ContentValues contentValues = new ContentValues();
            contentValues.put(MoviesContentProvider.KEY_ID, mMovieId);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_TITLE, mMovieTitle);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_BACKDROP, backDropPath);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_OVERVIEW, mMoviePlot);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_POSTER, posterPath);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_RELEASE_DATE, mMovieReleaseDate);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_USER_RATING, mMovieUserRating);
            Uri uri = getContentResolver().insert(MoviesContentProvider.uri, contentValues);
        } else if (imageButton.getTag().equals("R.drawable.favorite_clicked")) {
            imageButton.setImageResource(R.drawable.ic_action_favorite);
            imageButton.setTag("R.drawable.favorite");
            Toast.makeText(this, "Movie removed from Favorites", Toast.LENGTH_SHORT).show();

            String url = String.valueOf(MoviesContentProvider.uri) + "/" + mMovieId;
            Uri queryUri = Uri.parse(url);
            int i = getContentResolver().delete(queryUri, null, null);
            if (i > 0) {
                MainActivityFragment.mDataSetChanged = true;
                ClearPosterGarbage garbagePoster = new ClearPosterGarbage(posterPath, mMovieId + ".png");
                MainActivity.postersToBeDeleted.add(garbagePoster);
                ClearPosterGarbage garbageBackDrop = new ClearPosterGarbage(backDropPath, mMovieId + "back.png");
                MainActivity.postersToBeDeleted.add(garbageBackDrop);
            }
        }
    }

    public class ClearPosterGarbage {
        String mPosterPath;
        String mFileName;

        public ClearPosterGarbage(String posterPath, String fileName) {
            mPosterPath = posterPath;
            mFileName = fileName;
        }
    }

    public void saveImage(Bitmap bitmap, String fileName) {
        File image = new File(getFilesDir(), fileName + ".png");
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}