package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.content.Intent;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    PopMoviesAdaptor mPopMoviesAdaptor = null;
    ArrayList<MovieDetailsObject> mMovieDetailsArrayList = null;
    String mFetchMoviesBaseUrl = null;
    final String mPopularityTag = "Popularity";
    final String mRatingTag = "Rating";
    String mSortCriteria = mPopularityTag;
    final String mApiKeyParam = "api_key";
    final String mKeyValue = "Key goes here";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSortCriteria = savedInstanceState.getString("sortcriteria");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView moviesGridView = (GridView) rootView.findViewById(R.id.main_grid_view);
        mMovieDetailsArrayList = new ArrayList<MovieDetailsObject>();
        mPopMoviesAdaptor = new PopMoviesAdaptor(getActivity());

        updateMovieGridView();

        moviesGridView.setAdapter(mPopMoviesAdaptor);
        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                MovieDetailsObject tempObj = mMovieDetailsArrayList.get(position);
                Intent movieDetailsIntent = new Intent(getActivity(), MovieDetails.class);
                movieDetailsIntent.putExtra("movieTitle", tempObj.mStrMovieTitle);
                movieDetailsIntent.putExtra("moviePosterFullPath", tempObj.mStrMoviePosterFullPath);
                movieDetailsIntent.putExtra("movieUserRating", tempObj.mStrMovieUserRating);
                movieDetailsIntent.putExtra("movieReleaseDate", tempObj.mStrMovieReleaseDate);
                movieDetailsIntent.putExtra("moviePlot", tempObj.mStrMoviePlot);
                movieDetailsIntent.putExtra("movieBackDropPath", tempObj.mStrMovieBackDropPath);
                startActivity(movieDetailsIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("sortcriteria", mSortCriteria);
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
                updateMovieGridView();
                return true;
            case R.id.menuSortRating:
                item.setChecked(true);
                mSortCriteria = mRatingTag;
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
        FetchMoviesTask movieDetails = new FetchMoviesTask();
        movieDetails.execute();
    }


    public class FetchMoviesTask extends AsyncTask {
        private String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected ArrayList<MovieDetailsObject> doInBackground(Object... params) {
            String moviesJsonStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {

                Uri builtUri = Uri.parse(mFetchMoviesBaseUrl)
                        .buildUpon()
                        .appendQueryParameter(mApiKeyParam, mKeyValue)
                        .build();
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream == null) {
                    return null;
                }

                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "ERROR", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "ERROR", e);
                    }
                }
            }
            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "ERROR", e);
                ;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result != null) {
                for (int i = 0; i < mMovieDetailsArrayList.size(); ++i) {
                    mPopMoviesAdaptor.add(mMovieDetailsArrayList.get(i));
                }
                mPopMoviesAdaptor.notifyDataSetChanged();
            } else {
                Log.e(LOG_TAG, "ERROR : Result Null");
            }
        }

        public ArrayList<MovieDetailsObject> getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            final String TMDB_RESULTS = "results";
            final String TMDB_MOVIE_TITLE = "original_title";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_PLOT = "overview";
            final String TMDB_USER_RATING = "vote_average";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_BACKDROP_PATH = "backdrop_path";
            String posterBasePath = "http://image.tmdb.org/t/p/w342/";
            String backDropBasePath = "http://image.tmdb.org/t/p/w500/";

            String movieTitle,
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
                movieTitle = singleMovieDetails.getString(TMDB_MOVIE_TITLE);
                moviePosterPath = posterBasePath + singleMovieDetails.getString(TMDB_POSTER_PATH);
                moviePlot = singleMovieDetails.getString(TMDB_PLOT);
                movieUserRating = singleMovieDetails.getString(TMDB_USER_RATING);
                movieReleaseDate = singleMovieDetails.getString(TMDB_RELEASE_DATE);
                movieBackdropPath = backDropBasePath + singleMovieDetails.getString(TMDB_BACKDROP_PATH);
                MovieDetailsObject movie = new MovieDetailsObject(
                        moviePosterPath,
                        movieTitle,
                        moviePlot,
                        movieUserRating,
                        movieReleaseDate,
                        movieBackdropPath);
                mMovieDetailsArrayList.add(movie);
            }
            return mMovieDetailsArrayList;
        }
    }

    public class MovieDetailsObject {

        String mStrMoviePosterFullPath,
                mStrMovieTitle,
                mStrMoviePlot,
                mStrMovieUserRating,
                mStrMovieReleaseDate,
                mStrMovieBackDropPath = null;

        MovieDetailsObject(String moviePosterFullPath,
                           String movieTitle,
                           String moviePlot,
                           String movieUserRating,
                           String movieReleaseDate,
                           String movieBackDropPath) {
            mStrMoviePosterFullPath = moviePosterFullPath;
            mStrMovieTitle = movieTitle;
            mStrMoviePlot = moviePlot;
            mStrMovieUserRating = movieUserRating;
            mStrMovieReleaseDate = movieReleaseDate;
            mStrMovieBackDropPath = movieBackDropPath;
        }
    }

    public class PopMoviesAdaptor extends ArrayAdapter<MovieDetailsObject> {
        Context mContext;

        PopMoviesAdaptor(Context context) {
            super(context, 0);
            mContext = context;
        }

        @Override
        public int getCount() {
            return mMovieDetailsArrayList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                imageView = (ImageView) convertView;
            }
            MovieDetailsObject tempMovieObject = mMovieDetailsArrayList.get(position);
            Picasso.with(mContext)
                    .load(tempMovieObject.mStrMoviePosterFullPath)
                    .into(imageView);
            return imageView;
        }
    }
}
