package com.neerajms99b.neeraj.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String mFetchMoviesBaseUrl = null;
    private static boolean mEmptyDatabase;
    private final static String mApiKeyParam = "api_key";
    private final static String mKeyValue = "Key goes here";
    private static MainActivity mCallBack;
    private NetorkReceiver mNetorkReceiver;

    public final static String mPopularityTag = "Popularity";
    public final static String mFavoritesTag = "Favorites";
    public final static String mRatingTag = "Rating";

    public static boolean mDataSetChanged;
    public static int mGridPosition;
    public static boolean mOffline;
    public static Context mContext;
    public static PopMoviesAdapter mPopMoviesAdapter = null;
    public static ArrayList<MovieDetailsParcelable> mMovieDetailsArrayList = null;
    public static String mSortCriteria = mPopularityTag;

    public static MenuItem mMenuItemClearAll;

    public MainActivityFragment() {
    }

    public static ArrayList<MovieDetailsParcelable> getMovieDetailsArrayList() {
        return mMovieDetailsArrayList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mEmptyDatabase = false;
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSortCriteria = savedInstanceState.getString("sortcriteria");
            mGridPosition = savedInstanceState.getInt("gridposition");
            mMovieDetailsArrayList = savedInstanceState.getParcelableArrayList("parcel");
            mOffline = savedInstanceState.getBoolean("isoffline");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mNetorkReceiver);
    }

    public class NetorkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isInternetOn(getContext())) {
                if (!mOffline) {
                    updateMovieGridView();
                }
            } else {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);
                Snackbar.make(coordinatorLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE).show();
            }
        }
    }

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

    @Override
    public void onResume() {
        super.onResume();
        mNetorkReceiver = new NetorkReceiver();
        getContext().registerReceiver(mNetorkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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

        if (MainActivityFragment.mSortCriteria.equals(MainActivityFragment.mFavoritesTag)
                && MainActivity.last) {
            mMenuItemClearAll.setVisible(false);
            if (mCallBack.isTwoPane()) {
                MainActivity.mFrameLayout.setVisibility(View.INVISIBLE);
            }
            Toast.makeText(getContext(),
                    "You have removed all the favorites, nothing to show here",
                    Toast.LENGTH_SHORT).show();
        }
        GridView moviesGridView = (GridView) rootView.findViewById(R.id.main_grid_view);
        mPopMoviesAdapter = new PopMoviesAdapter(getActivity());

        if (savedInstanceState == null && !mOffline) {
            mMovieDetailsArrayList = new ArrayList<MovieDetailsParcelable>();
            updateMovieGridView();
        }

        if (mMovieDetailsArrayList.isEmpty() && mSortCriteria.equals(mFavoritesTag)) {
            mOffline = true;
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

    /*Fetching trailers from themoviedb*/
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

    /*Extracting trailers from JSON result*/
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
        outState.putBoolean("isoffline",mOffline);
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
        mMenuItemClearAll = (MenuItem) menu.findItem(R.id.clear_all);

        mMenuItemClearAll.setVisible(false);
        if (mSortCriteria.equals(mPopularityTag)) {
            if (!menuItemSortPopularity.isChecked()) {
                menuItemSortPopularity.setChecked(true);
            }
            mMenuItemClearAll.setVisible(false);
        } else if (mSortCriteria.equals(mRatingTag)) {
            if (!menuItemSortRating.isChecked()) {
                menuItemSortRating.setChecked(true);
            }
            mMenuItemClearAll.setVisible(false);
        } else if (mSortCriteria.equals(mFavoritesTag)) {
            if (!menuItemFavorites.isChecked()) {
                menuItemFavorites.setChecked(true);
            }
            if (!MainActivity.last) {
                mMenuItemClearAll.setVisible(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuFavorites:
                if (!MainActivity.last) {
                    mMenuItemClearAll.setVisible(true);
                }
                int tempGridPosition;
                tempGridPosition = mGridPosition;
                mGridPosition = 0;
                updateGridOffline();
                if (!mEmptyDatabase) {
                    item.setChecked(true);
                    mSortCriteria = mFavoritesTag;
                    mOffline = true;
                } else {
                    Toast.makeText(getContext(),
                            "Looks like you have no favorites yet",
                            Toast.LENGTH_SHORT).show();
                    mMenuItemClearAll.setVisible(false);
                    mEmptyDatabase = false;
                    mGridPosition = tempGridPosition;
                }
                return true;

            case R.id.menuSortPopularity:
                MainActivity.last = false;
                item.setChecked(true);
                mMenuItemClearAll.setVisible(false);
                mCallBack.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                mGridPosition = 0;
                mSortCriteria = mPopularityTag;
                mOffline = false;
                updateMovieGridView();
                mMovieDetailsArrayList.clear();
                mPopMoviesAdapter.notifyDataSetChanged();
                if (mCallBack.isTwoPane()) {
                    MainActivity.mFrameLayout.setVisibility(View.VISIBLE);
                }
                return true;

            case R.id.menuSortRating:
                item.setChecked(true);
                mMenuItemClearAll.setVisible(false);
                MainActivity.last = false;
                mGridPosition = 0;
                mCallBack.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                mSortCriteria = mRatingTag;
                mOffline = false;
                updateMovieGridView();
                mMovieDetailsArrayList.clear();
                mPopMoviesAdapter.notifyDataSetChanged();
                if (mCallBack.isTwoPane()) {
                    MainActivity.mFrameLayout.setVisibility(View.VISIBLE);
                }
                return true;

            case R.id.clear_all:
                clearAllFavorites();
                mMenuItemClearAll.setVisible(false);
                if (mCallBack.isTwoPane()) {
                    MainActivity.mFrameLayout.setVisibility(View.INVISIBLE);
                }
                MainActivity.last = true;
                Toast.makeText(getContext(), "You have removed all the favorites, nothing to show here",
                        Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*To clear all favorites*/
    private void clearAllFavorites() {
        Cursor c = mContext.getContentResolver().query(MoviesContentProvider.mUri,
                null, null, null, null);
        String posterPath = mContext.getFilesDir().getAbsolutePath();
        String movieId;

        if (c.moveToFirst()) {
            do {
                movieId = c.getString(c.getColumnIndex(MoviesContentProvider.KEY_ID));
                File posterFile = new File(posterPath, movieId + ".png");
                boolean deletedPoster = posterFile.delete();
                File backDropFile = new File(posterPath, movieId + "back.png");
                boolean deletedBackDrop = backDropFile.delete();
            } while (c.moveToNext());
        }

        int deleteCount = mContext.getContentResolver().delete(MoviesContentProvider.mUri, null, null);
        mMovieDetailsArrayList.clear();
        mPopMoviesAdapter.notifyDataSetChanged();
    }

    public static void updateGridOffline() {
        Cursor c = mContext.getContentResolver().query(MoviesContentProvider.mUri,
                null, null, null, null);

        String movieId,
                movieTitle,
                moviePosterPath,
                moviePlot,
                movieUserRating,
                movieBackdropPath,
                movieReleaseDate = null;

        if (c.moveToFirst()) {
            mEmptyDatabase = false;
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
            mEmptyDatabase = true;
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

            if (mCallBack.isTwoPane()) {
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