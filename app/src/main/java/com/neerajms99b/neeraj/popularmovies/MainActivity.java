package com.neerajms99b.neeraj.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnGridItemSelectedListener {
    private boolean mTwoPane = false;
    private String mMovieId;
    private String mMovieTitle;
    private String mMoviePosterFullPath;
    private String mMovieUserRating;
    private String mMovieReleaseDate;
    private String mMoviePlot;
    private String mMovieBackDropPath;

    private MovieDetailsParcelable movieDetailsParcelable;

    public static FrameLayout mFrameLayout;

    private static boolean last = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        if (!isInternetOn(this)) {
            Snackbar.make(coordinatorLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE).show();
        }

        if (this.findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }

        if (MainActivityFragment.mMovieDetailsArrayList.size() > 0) {
            last = false;
        }

        mFrameLayout = (FrameLayout) findViewById(R.id.movie_detail_container);
        if (MainActivityFragment.mSortCriteria.equals(MainActivityFragment.mFavoritesTag) && last == true && mTwoPane == true) {
            mFrameLayout.setVisibility(View.INVISIBLE);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey("parcel") && mTwoPane) {
            movieDetailsParcelable = savedInstanceState.getParcelable("parcel");
            mMovieId = movieDetailsParcelable.mMovieId;
            mMovieTitle = movieDetailsParcelable.mMovieTitle;
            mMoviePlot = movieDetailsParcelable.mMoviePlot;
            mMoviePosterFullPath = movieDetailsParcelable.mMoviePosterFullPath;
            mMovieReleaseDate = movieDetailsParcelable.mMovieReleaseDate;
            mMovieUserRating = movieDetailsParcelable.mMovieUserRating;
            mMovieBackDropPath = movieDetailsParcelable.mMovieBackDropPath;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMovieId != null) {
            outState.putParcelable("parcel", movieDetailsParcelable);
        }
    }

    @Override
    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public void onMovieSelected(String movieId,
                                String movieTitle,
                                String moviePosterFullPath,
                                String movieUserRating,
                                String movieReleaseDate,
                                String moviePlot,
                                String movieBackDropPath) {
        mMovieId = movieId;
        mMovieTitle = movieTitle;
        mMoviePosterFullPath = moviePosterFullPath;
        mMovieUserRating = movieUserRating;
        mMovieReleaseDate = movieReleaseDate;
        mMoviePlot = moviePlot;
        mMovieBackDropPath = movieBackDropPath;
        movieDetailsParcelable = new MovieDetailsParcelable(
                mMovieId,
                mMovieTitle,
                mMoviePosterFullPath,
                mMovieUserRating,
                mMovieReleaseDate,
                mMoviePlot,
                mMovieBackDropPath
        );
        if (mTwoPane == true) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container,
                            MovieDetailsFragment.newInstance(
                                    mMovieId,
                                    mMovieTitle,
                                    mMoviePosterFullPath,
                                    mMovieUserRating,
                                    mMovieReleaseDate,
                                    mMoviePlot,
                                    mMovieBackDropPath), "MovieDetailsFragment").commit();
        } else {
            Intent movieDetailsIntent = new Intent(this, MovieDetailsActivity.class);
            movieDetailsIntent.putExtra("movieId", mMovieId);
            movieDetailsIntent.putExtra("movieTitle", mMovieTitle);
            movieDetailsIntent.putExtra("moviePosterFullPath", mMoviePosterFullPath);
            movieDetailsIntent.putExtra("movieUserRating", mMovieUserRating);
            movieDetailsIntent.putExtra("movieReleaseDate", mMovieReleaseDate);
            movieDetailsIntent.putExtra("moviePlot", mMoviePlot);
            movieDetailsIntent.putExtra("movieBackDropPath", mMovieBackDropPath);
            startActivity(movieDetailsIntent);
        }
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

    public void favoriteMovie(View view) {
        String posterPath = getFilesDir().getAbsolutePath();
        ImageButton imageButton = (ImageButton) findViewById(R.id.favorite_button);

        if (mMovieId != null) {
            if (imageButton.getTag().equals("R.drawable.favorite")) {
                imageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
                imageButton.setTag("R.drawable.favorite_clicked");
                Toast.makeText(this, "Movie added to Favorites", Toast.LENGTH_SHORT).show();
                MainActivityFragment.mDataSetChanged = false;

                Picasso.with(view.getContext())
                        .load(mMoviePosterFullPath)
                        .into(new Target() {
                                  @Override
                                  public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                      try {
                                          File image = new File(getFilesDir(), mMovieId + ".png");
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
                                      } catch (Exception e) {
                                          Log.d("Error:", "could not open file");
                                      }
                                  }

                                  @Override
                                  public void onBitmapFailed(Drawable errorDrawable) {
                                  }

                                  @Override
                                  public void onPrepareLoad(Drawable placeHolderDrawable) {
                                  }
                              }
                        );

                ContentValues contentValues = new ContentValues();
                contentValues.put(MoviesContentProvider.KEY_ID, mMovieId);
                contentValues.put(MoviesContentProvider.KEY_MOVIE_TITLE, mMovieTitle);
                contentValues.put(MoviesContentProvider.KEY_MOVIE_BACKDROP, posterPath);
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
                    File posterFile = new File(posterPath, mMovieId + ".png");
                    boolean deletedPoster = posterFile.delete();
                }

                if (isTwoPane() && MainActivityFragment.mSortCriteria.equals(MainActivityFragment.mFavoritesTag)) {
                    MainActivityFragment.mMovieDetailsArrayList.clear();
                    if (MainActivityFragment.mGridPosition > 1) {
                        MainActivityFragment.mGridPosition = MainActivityFragment.mGridPosition - 1;
                    } else {
                        MainActivityFragment.mGridPosition = 0;
                    }
                    MainActivityFragment.offline = true;
                    MainActivityFragment.updateGridOffline();
                    MainActivityFragment.mPopMoviesAdapter.notifyDataSetChanged();
                }
                if (MainActivityFragment.mMovieDetailsArrayList.size() == 0 && mTwoPane == true) {
                    last = true;
                    mFrameLayout.setVisibility(View.INVISIBLE);
                } else {
                    last = false;
                }
            }
        } else {
            Toast.makeText(this, "Please select a movie", Toast.LENGTH_SHORT).show();
        }
    }
}
