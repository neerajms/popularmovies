package com.neerajms99b.neeraj.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnGridItemSelectedListener {
    boolean mTwoPane = false;
    String mMovieId;
    String mMovieTitle;
    String mMoviePosterFullPath;
    String mMovieUserRating;
    String mMovieReleaseDate;
    String mMoviePlot;
    String mMovieBackDropPath;
    Bundle mSavedInstance;
    String mTrailerKey;


    Bitmap mBitmapPoster;
    //    Bitmap mBitmapBackDrop;
    MainActivityFragment mCallBackMovieDetailsFragment;
    static String posterPath;

    //    static String backDropPath;
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
        if (this.findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
//            if (savedInstanceState == null) {
//            Log.d("MOVIE TITLE",mMovieTitle);

//            }
//            Log.d("Found:", "two pane detected");
        } else {
            mTwoPane = false;

        }


    }

    @Override
    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    public void onMovieSelected(String movieId, String movieTitle, String moviePosterFullPath, String movieUserRating, String movieReleaseDate, String moviePlot, String movieBackDropPath) {
//        mTrailerKey = trailerKey;
        mMovieId = movieId;
        mMovieTitle = movieTitle;
        mMoviePosterFullPath = moviePosterFullPath;
        mMovieUserRating = movieUserRating;
        mMovieReleaseDate = movieReleaseDate;
        mMoviePlot = moviePlot;
        mMovieBackDropPath = movieBackDropPath;
String dummy = null;
        if (mTwoPane == true) {
//            if (mSavedInstance == null) {
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
//            }
        } else {
            Intent movieDetailsIntent = new Intent(this, MovieDetailsActivity.class);
//            movieDetailsIntent.putExtra("trailer",mTrailerKey);
            movieDetailsIntent.putExtra("movieId", mMovieId);
            movieDetailsIntent.putExtra("movieTitle", mMovieTitle);
            movieDetailsIntent.putExtra("moviePosterFullPath", mMoviePosterFullPath);
            movieDetailsIntent.putExtra("movieUserRating", mMovieUserRating);
            movieDetailsIntent.putExtra("movieReleaseDate", mMovieReleaseDate);
            movieDetailsIntent.putExtra("moviePlot", mMoviePlot);
            movieDetailsIntent.putExtra("movieBackDropPath", mMovieBackDropPath);
            startActivity(movieDetailsIntent);
        }

//        Log.d("title::", mMovieTitle);
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
        posterPath = getFilesDir().getAbsolutePath();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
//        backDropPath = getFilesDir().getAbsolutePath();
        ImageButton imageButton = (ImageButton)findViewById(R.id.favorite_button);

        if (imageButton.getTag().equals("R.drawable.favorite")) {

            imageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
            imageButton.setTag("R.drawable.favorite_clicked");
            Toast.makeText(this, "Movie favorited", Toast.LENGTH_SHORT).show();
            MainActivityFragment.mDataSetChanged = false;

//
//            ImageView posterImageView = (ImageView)findViewById(R.id.movie_poster_image_view);
//            BitmapDrawable posterBitmapDrawable = (BitmapDrawable)posterImageView.getDrawable();
//            Bitmap bitmapPoster = posterBitmapDrawable.getBitmap();
//
//            ImageView backDropImageView = (ImageView)findViewById(R.id.back_drop_image);
//            BitmapDrawable backDropBitmapDrawable = (BitmapDrawable)backDropImageView.getDrawable();
//            Bitmap bitmapBackDrop = backDropBitmapDrawable.getBitmap();

            Picasso.with(view.getContext())
                    .load(mMoviePosterFullPath)
                    .into(new Target() {
                              @Override
                              public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                  try {
                                      File image =new File(getFilesDir(),mMovieId+".png");
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
//                                          Log.d("File Operation:",mMovieId+".png");
                                      } catch (FileNotFoundException e) {
                                          e.printStackTrace();
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }
                                  } catch(Exception e){
                                      // some action
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

//            saveImage(bitmapPoster,mMovieId);
//            saveImage(bitmapBackDrop,mMovieId+"back");
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
            contentValues.put(MoviesContentProvider.KEY_ID,mMovieId);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_TITLE,mMovieTitle);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_BACKDROP,posterPath);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_OVERVIEW,mMoviePlot);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_POSTER,posterPath);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_RELEASE_DATE,mMovieReleaseDate);
            contentValues.put(MoviesContentProvider.KEY_MOVIE_USER_RATING,mMovieUserRating);
            Uri uri = getContentResolver().insert(MoviesContentProvider.uri,contentValues);
//            Log.d("saved",String.valueOf(uri));
//            if(isTwoPane() && MainActivityFragment.noNetwork){
////                MainActivityFragment.mMovieDetailsArrayList.clear();
//                MainActivityFragment.updateGridOffline();
//                MainActivityFragment.mPopMoviesAdapter.notifyDataSetChanged();
//            }

        } else if (imageButton.getTag().equals("R.drawable.favorite_clicked")) {

            imageButton.setImageResource(R.drawable.ic_action_favourite);
            imageButton.setTag("R.drawable.favorite");
            Toast.makeText(this, "Movie unfavorited", Toast.LENGTH_SHORT).show();
            String url = String.valueOf(MoviesContentProvider.uri)+"/"+mMovieId;
            Uri queryUri = Uri.parse(url);
//            Log.d("url to delete:::::",String.valueOf(queryUri));
            int i = getContentResolver().delete(queryUri,null,null);
//            Log.d("Value of i",String.valueOf(i));
            if(i > 0) {
                MainActivityFragment.mDataSetChanged = true;
//                Log.d("Poster path::::::",posterPath);
                File posterFile = new File(posterPath, mMovieId+".png");
                boolean deletedPoster = posterFile.delete();
//                File backDropFile = new File(backDropPath, mMovieId+"back.png");
//                boolean deletedBackDrop = backDropFile.delete();
                if (deletedPoster){
//                    Log.d("POSTER status:", "deleted");
                }
//                if (deletedBackDrop){
//                    Log.d("Backdrop status:", "deleted");
//                }
            }
            int indexToRemove = 0;

            if (isTwoPane() && MainActivityFragment.mSortCriteria.equals("Favorites")) {
                MainActivityFragment.mMovieDetailsArrayList.clear();
                if (MainActivityFragment.mGridPosition > 1) {
                   MainActivityFragment.mGridPosition = MainActivityFragment.mGridPosition - 1;
                } else {
                    MainActivityFragment.mGridPosition = 0;
                }
                MainActivityFragment.noNetwork = true;
                MainActivityFragment.updateGridOffline();
                if (MainActivityFragment.mGridPosition > 0) {
                    MainActivityFragment.mPopMoviesAdapter.notifyDataSetChanged();
                }else {
//                    FrameLayout frameLayout = (FrameLayout)findViewById(R.id.movie_detail_container);
//                    frameLayout.setVisibility(View.INVISIBLE);
                    imageButton.setVisibility(View.INVISIBLE);
                }
            }
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    public void saveImage(Bitmap bitmap,String fileName){
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        File myDir = cw.getDir("movies", Context.MODE_PRIVATE);
//        if (!myDir.exists()) {
//            myDir.mkdir();
//        }
        File image =new File(getFilesDir(),fileName+".png");
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
//            Log.d("File Operation:",mMovieId+".png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
