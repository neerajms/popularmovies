package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;

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
 * public class PopMoviesAdapter {
 * }
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    PopMoviesAdapter mPopMoviesAdapter = null;
    static ArrayList<MovieDetailsParcelable> mMovieDetailsArrayList = null;
    String mFetchMoviesBaseUrl = null;
    final String mPopularityTag = "Popularity";
    final String mRatingTag = "Rating";
    String mSortCriteria = mPopularityTag;
    final String mApiKeyParam = "api_key";
    final String mKeyValue = "2b34f0a753ed8e38b7546773dbed2720";
    MainActivity mCallBack;
    boolean twoPane;
    int mGridPosition;
    Bundle mSavedInstanceState;
//    MovieDetailsParcelable[] movieDetailsParcelables;

    public MainActivityFragment() {
    }


    public static ArrayList<MovieDetailsParcelable> getMovieDetailsArrayList() {
        return mMovieDetailsArrayList;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSortCriteria = savedInstanceState.getString("sortcriteria");
            mGridPosition = savedInstanceState.getInt("gridposition");
//            movieDetailsParcelables = (MovieDetailsParcelable[]) savedInstanceState.getParcelableArray("parcel");
            mMovieDetailsArrayList = savedInstanceState.getParcelableArrayList("parcel");
        }
        setHasOptionsMenu(true);
    }

    public interface OnGridItemSelectedListener {
        public void onMovieSelected(String movieId,
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
        mSavedInstanceState = savedInstanceState;
        GridView moviesGridView = (GridView) rootView.findViewById(R.id.main_grid_view);
        mPopMoviesAdapter = new PopMoviesAdapter(getActivity());

        if (savedInstanceState == null) {
            mMovieDetailsArrayList = new ArrayList<MovieDetailsParcelable>();

            updateMovieGridView();
        }

        moviesGridView.setAdapter(mPopMoviesAdapter);
        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                mGridPosition = position;
                MovieDetailsParcelable tempObj = mMovieDetailsArrayList.get(position);

                mCallBack.onMovieSelected(tempObj.mMovieId,
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

        if (mSortCriteria.equals(mPopularityTag)) {
            if (!menuItemSortPopularity.isChecked()) {
                menuItemSortPopularity.setChecked(true);
            }
        } else if (mSortCriteria.equals(mRatingTag)) {
            if (!menuItemSortRating.isChecked()) {
                menuItemSortRating.setChecked(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuSortPopularity:
                item.setChecked(true);
                mSortCriteria = mPopularityTag;
                mGridPosition = 0;
                updateMovieGridView();
                return true;
            case R.id.menuSortRating:
                item.setChecked(true);
                mSortCriteria = mRatingTag;
                mGridPosition = 0;
                updateMovieGridView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateMovieGridView() {
        if (mSortCriteria.equals(mPopularityTag)) {
            mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/popular?";
        } else if (mSortCriteria.equals(mRatingTag)) {
            mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/top_rated?";
        }

        // Instantiate the RequestQueue.
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


//        FetchMoviesTask movieDetails = new FetchMoviesTask();
//        movieDetails.execute();
    }

    protected void onPostExecute() {

        for (int i = 0; i < mMovieDetailsArrayList.size(); ++i) {
            mPopMoviesAdapter.add(mMovieDetailsArrayList.get(i));
//                    movieDetailsParcelables[i] = new MovieDetailsParcelable(mMovieDetailsArrayList.get(i).mStrMovieTitle,
//                            mMovieDetailsArrayList.get(i).mStrMoviePosterFullPath,
//                            mMovieDetailsArrayList.get(i).mStrMovieUserRating,
//                            mMovieDetailsArrayList.get(i).mStrMovieReleaseDate,
//                            mMovieDetailsArrayList.get(i).mStrMoviePlot,
//                            mMovieDetailsArrayList.get(i).mStrMovieBackDropPath);
            Log.d("id",mMovieDetailsArrayList.get(i).mMovieId);

        }
        mPopMoviesAdapter.notifyDataSetChanged();
        if (mCallBack.isTwoPane()) {
            MovieDetailsParcelable tempObj = mMovieDetailsArrayList.get(mGridPosition);

            mCallBack.onMovieSelected(tempObj.mMovieId,
                    tempObj.mMovieTitle,
                    tempObj.mMoviePosterFullPath,
                    tempObj.mMovieUserRating,
                    tempObj.mMovieReleaseDate,
                    tempObj.mMoviePlot,
                    tempObj.mMovieBackDropPath);
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
//            Log.d("MovieTitle:", mMovieDetailsArrayList.get(i).mMovieTitle);

        }

        return mMovieDetailsArrayList;
    }
}