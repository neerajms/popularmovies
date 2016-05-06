package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public static PopMoviesAdapter mPopMoviesAdapter = null;
    public static ArrayList<MovieDetailsParcelable> mMovieDetailsArrayList = null;
    private String mFetchMoviesBaseUrl = null;
    public final static String mPopularityTag = "Popularity";
    public final static String mFavoritesTag = "Favorites";
    public final static String mRatingTag = "Rating";
    public static String mSortCriteria = mPopularityTag;
    private final static String mApiKeyParam = "api_key";
    private final static String mKeyValue = "2b34f0a753ed8e38b7546773dbed2720";
    private static MainActivity mCallBack;
    public static boolean mDataSetChanged;
    public static int mGridPosition;
    public static boolean noNetwork;
    public static Context mContext;
    private static boolean emptyDatabase;

    public MainActivityFragment() {
    }

    public static ArrayList<MovieDetailsParcelable> getMovieDetailsArrayList() {
        return mMovieDetailsArrayList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        emptyDatabase = false;
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSortCriteria = savedInstanceState.getString("sortcriteria");
            mGridPosition = savedInstanceState.getInt("gridposition");
            mMovieDetailsArrayList = savedInstanceState.getParcelableArrayList("parcel");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDataSetChanged && mSortCriteria.equals(mFavoritesTag)) {
            mMovieDetailsArrayList.clear();
            updateGridOffline();
            mPopMoviesAdapter.notifyDataSetChanged();
            mDataSetChanged = false;
        }
    }

    public interface OnGridItemSelectedListener {
        public void onMovieSelected(
                String movieId,
                String movieTitle,
                String moviePosterFullPath,
                String movieUserRating,
                String movieReleaseDate,
                String moviePlot,
                String movieBackDropPath);

        public boolean isTwoPane();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mCallBack = (MainActivity) getActivity();
        mContext = getContext();

        GridView moviesGridView = (GridView) rootView.findViewById(R.id.main_grid_view);
        mPopMoviesAdapter = new PopMoviesAdapter(getActivity());

        if (savedInstanceState == null) {
            mMovieDetailsArrayList = new ArrayList<MovieDetailsParcelable>();
            updateMovieGridView();
        }

        if (mMovieDetailsArrayList.isEmpty() && mSortCriteria.equals(mFavoritesTag)) {
            noNetwork = true;
            mMovieDetailsArrayList = new ArrayList<MovieDetailsParcelable>();
            updateGridOffline();
        }

        moviesGridView.setAdapter(mPopMoviesAdapter);
        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mGridPosition = position;
                MovieDetailsParcelable tempObj = mMovieDetailsArrayList.get(position);
                mCallBack.onMovieSelected(
                        tempObj.mMovieId,
                        tempObj.mMovieTitle,
                        tempObj.mMoviePosterFullPath,
                        tempObj.mMovieUserRating,
                        tempObj.mMovieReleaseDate,
                        tempObj.mMoviePlot,
                        tempObj.mMovieBackDropPath);
            }
        });

        return rootView;
    }

    public static void fetchTrailers(final String movieId) {
        String fetchTrailersUrl = "https://api.themoviedb.org/3/movie/" + movieId + "/" + "videos";
        Uri builtUri = Uri.parse(fetchTrailersUrl)
                .buildUpon()
                .appendQueryParameter(mApiKeyParam, mKeyValue)
                .build();
        String url = builtUri.toString();

        RequestQueue queue = Volley.newRequestQueue(mContext);
        FetchDataTask fetchMoviesTask = new FetchDataTask();
        VolleyCallBack volleyCallBack = new VolleyCallBack() {
            @Override
            public void returnResponse(String result) {
                getTrailersFromJson(movieId, result);
            }
        };
        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
    }

    public static void getTrailersFromJson(String movieId, String jsonResult) {
        String trailerKey = null;
        final String TMDB_RESULTS = "results";
        final String TMDB_TRAILER_KEY = "key";
        try {
            JSONObject trailersJsonObject = new JSONObject(jsonResult);
            JSONArray trailersArray = trailersJsonObject.getJSONArray(TMDB_RESULTS);
            trailerKey = trailersArray.getJSONObject(0).getString(TMDB_TRAILER_KEY);

            SharedPreferences sharedPref = mContext.getSharedPreferences(
                    "trailerShared", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(movieId, trailerKey);
            editor.commit();
        } catch (JSONException je) {
            Log.d("Error:", "Could not extract trailers from JSON");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("sortcriteria", mSortCriteria);
        outState.putInt("gridposition", mGridPosition);
        outState.putParcelableArrayList("parcel", mMovieDetailsArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem menuItemSort = (MenuItem) menu.findItem(R.id.menuSortPopularity);
        MenuItem menuItemSortPopularity = (MenuItem) menu.findItem(R.id.menuSortPopularity);
        MenuItem menuItemSortRating = (MenuItem) menu.findItem(R.id.menuSortRating);
        MenuItem menuItemFavorites = (MenuItem) menu.findItem(R.id.menuFavorites);

        if (mSortCriteria.equals(mPopularityTag)) {
            if (!menuItemSortPopularity.isChecked()) {
                menuItemSortPopularity.setChecked(true);
            }
        } else if (mSortCriteria.equals(mRatingTag)) {
            if (!menuItemSortRating.isChecked()) {
                menuItemSortRating.setChecked(true);
            }
        } else if (mSortCriteria.equals(mFavoritesTag)) {
            if (!menuItemFavorites.isChecked()) {
                menuItemFavorites.setChecked(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuFavorites:
                if (mCallBack.isTwoPane()) {
                    MainActivity.mFrameLayout.setVisibility(View.VISIBLE);
                }
                int tempGridPosition;
                tempGridPosition = mGridPosition;
                mGridPosition = 0;
                updateGridOffline();
                if (!emptyDatabase) {
                    item.setChecked(true);
                    String tempSortCriteria = mSortCriteria;
                    mSortCriteria = mFavoritesTag;
                    noNetwork = true;
                } else {
                    Toast.makeText(getContext(), "Looks like you have no favorites yet", Toast.LENGTH_SHORT).show();
                    emptyDatabase = false;
                    mGridPosition = tempGridPosition;
                }
                return true;
            case R.id.menuSortPopularity:
                if (mCallBack.isTwoPane()) {
                    MainActivity.mFrameLayout.setVisibility(View.VISIBLE);
                }
                item.setChecked(true);
                mCallBack.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                mGridPosition = 0;
                mSortCriteria = mPopularityTag;
                noNetwork = false;
                updateMovieGridView();
                mMovieDetailsArrayList.clear();
                mPopMoviesAdapter.notifyDataSetChanged();
                return true;
            case R.id.menuSortRating:
                item.setChecked(true);
                if (mCallBack.isTwoPane()) {
                    MainActivity.mFrameLayout.setVisibility(View.VISIBLE);
                }
                mGridPosition = 0;
                mCallBack.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                mSortCriteria = mRatingTag;
                noNetwork = false;
                updateMovieGridView();
                mMovieDetailsArrayList.clear();
                mPopMoviesAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void updateGridOffline() {
        Cursor c = mContext.getContentResolver().query(MoviesContentProvider.uri,
                null, null, null, null);
        String movieId,
                movieTitle,
                moviePosterPath,
                moviePlot,
                movieUserRating,
                movieBackdropPath,
                movieReleaseDate = null;
        int index = 0;
        if (c.moveToFirst()) {
            emptyDatabase = false;
            mMovieDetailsArrayList.clear();
            mPopMoviesAdapter.notifyDataSetChanged();

            do {
                movieId = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_ID));
                movieTitle = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_MOVIE_TITLE));
                moviePosterPath = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_MOVIE_POSTER));
                movieBackdropPath = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_MOVIE_BACKDROP));
                moviePlot = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_MOVIE_OVERVIEW));
                movieReleaseDate = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_MOVIE_RELEASE_DATE));
                movieUserRating = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_MOVIE_USER_RATING));
                MovieDetailsParcelable movie = new MovieDetailsParcelable(movieId,
                        movieTitle,
                        moviePosterPath,
                        movieUserRating,
                        movieReleaseDate,
                        moviePlot,
                        movieBackdropPath);
                mMovieDetailsArrayList.add(movie);
            } while (c.moveToNext());
            onPostExecute();
        } else {
            emptyDatabase = true;
        }
    }

    public void updateMovieGridView() {
        if (mSortCriteria.equals(mPopularityTag)) {
            mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/popular?";
        } else if (mSortCriteria.equals(mRatingTag)) {
            mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/top_rated?";
        }

        Uri builtUri = Uri.parse(mFetchMoviesBaseUrl)
                .buildUpon()
                .appendQueryParameter(mApiKeyParam, mKeyValue)
                .build();
        String url = builtUri.toString();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        FetchDataTask fetchMoviesTask = new FetchDataTask();
        VolleyCallBack volleyCallBack = new VolleyCallBack() {
            @Override
            public void returnResponse(String result) {
                try {
                    getMoviesDataFromJson(result);
                    onPostExecute();
                } catch (JSONException je) {

                }
            }
        };
        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
    }

    protected static void onPostExecute() {
        if (!mMovieDetailsArrayList.isEmpty()) {
            for (int i = 0; i < mMovieDetailsArrayList.size(); ++i) {
                mPopMoviesAdapter.add(mMovieDetailsArrayList.get(i));
            }
            mPopMoviesAdapter.notifyDataSetChanged();

            if (mCallBack.isTwoPane()) {//&& mSortCriteria.equals(mFavoritesTag)
                MovieDetailsParcelable tempObj = mMovieDetailsArrayList.get(mGridPosition);
                fetchTrailers(tempObj.mMovieId);
                mCallBack.onMovieSelected(
                        tempObj.mMovieId,
                        tempObj.mMovieTitle,
                        tempObj.mMoviePosterFullPath,
                        tempObj.mMovieUserRating,
                        tempObj.mMovieReleaseDate,
                        tempObj.mMoviePlot,
                        tempObj.mMovieBackDropPath);
            }
        }
    }

    public ArrayList<MovieDetailsParcelable> getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {
        final String TMDB_MOVIE_ID = "id";
        final String TMDB_RESULTS = "results";
        final String TMDB_MOVIE_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_PLOT = "overview";
        final String TMDB_USER_RATING = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_BACKDROP_PATH = "backdrop_path";
        String posterBasePath = "http://image.tmdb.org/t/p/w342/";
        String backDropBasePath = "http://image.tmdb.org/t/p/w500/";

        String movieId,
                movieTitle,
                moviePosterPath,
                moviePlot,
                movieUserRating,
                movieBackdropPath,
                movieReleaseDate = null;

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);
        mMovieDetailsArrayList.clear();

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject singleMovieDetails = moviesArray.getJSONObject(i);
            movieId = singleMovieDetails.getString(TMDB_MOVIE_ID);
            fetchTrailers(movieId);
            movieTitle = singleMovieDetails.getString(TMDB_MOVIE_TITLE);
            moviePosterPath = posterBasePath + singleMovieDetails.getString(TMDB_POSTER_PATH);
            moviePlot = singleMovieDetails.getString(TMDB_PLOT);
            movieUserRating = singleMovieDetails.getString(TMDB_USER_RATING);
            movieReleaseDate = singleMovieDetails.getString(TMDB_RELEASE_DATE);
            movieBackdropPath = backDropBasePath + singleMovieDetails.getString(TMDB_BACKDROP_PATH);
            MovieDetailsParcelable movie = new MovieDetailsParcelable(movieId,
                    movieTitle,
                    moviePosterPath,
                    movieUserRating,
                    movieReleaseDate,
                    moviePlot,
                    movieBackdropPath);
            mMovieDetailsArrayList.add(movie);
        }
        return mMovieDetailsArrayList;
    }
}