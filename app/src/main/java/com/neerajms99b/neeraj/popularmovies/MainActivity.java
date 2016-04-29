package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnGridItemSelectedListener {
    boolean mTwoPane = false;

    String mMovieTitle;
    String mMoviePosterFullPath;
    String mMovieUserRating;
    String mMovieReleaseDate;
    String mMoviePlot;
    String mMovieBackDropPath;
    Bundle mSavedInstance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View.OnClickListener onClickListener;
        final Context context = this;
        super.onCreate(savedInstanceState);
        mSavedInstance = savedInstanceState;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        /*Show a message if there is no internet*/
        if (!isInternetOn(this)) {


            Snackbar.make(coordinatorLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE).show();



        }
        if (this.findViewById(R.id.movie_detail_container)!=null){
            mTwoPane = true;
//            if (savedInstanceState == null) {
//            Log.d("MOVIE TITLE",mMovieTitle);

//            }
            Log.d("Found:","two pane detected");
        }else {
            mTwoPane = false;

        }



    }

    @Override
    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public void onMovieSelected(String movieTitle, String moviePosterFullPath, String movieUserRating, String movieReleaseDate, String moviePlot, String movieBackDropPath) {
        mMovieTitle = movieTitle;
        mMoviePosterFullPath = moviePosterFullPath;
        mMovieUserRating = movieUserRating;
        mMovieReleaseDate=movieReleaseDate;
        mMoviePlot=moviePlot;
        mMovieBackDropPath=movieBackDropPath;
        if (mTwoPane == true) {
//            if (mSavedInstance == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container,
                                MovieDetailsFragment.newInstance(mMovieTitle,
                                        mMoviePosterFullPath,
                                        mMovieUserRating,
                                        mMovieReleaseDate,
                                        mMoviePlot,
                                        mMovieBackDropPath), "MovieDetailsFragment").commit();
//            }
        }else {
            Intent movieDetailsIntent = new Intent(this, MovieDetails.class);
            movieDetailsIntent.putExtra("movieTitle", mMovieTitle);
            movieDetailsIntent.putExtra("moviePosterFullPath", mMoviePosterFullPath);
            movieDetailsIntent.putExtra("movieUserRating", mMovieUserRating);
            movieDetailsIntent.putExtra("movieReleaseDate", mMovieReleaseDate);
            movieDetailsIntent.putExtra("moviePlot", mMoviePlot);
            movieDetailsIntent.putExtra("movieBackDropPath", mMovieBackDropPath);
            startActivity(movieDetailsIntent);
        }

        Log.d("title::",mMovieTitle);
    }
    /*Method to check if there is internet connection*/

    public boolean isInternetOn(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            return false;
        }
        return true;
    }
}
