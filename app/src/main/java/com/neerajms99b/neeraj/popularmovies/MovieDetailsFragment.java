package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment {
    String mMovieId;
    String mMoviePosterPath;
    String mMovieUserRating;
    String mMovieReleaseDate;
    String mMoviePlot;
    String mMovieTitle;
    String mMovieBackDropPath;

    ArrayList<String> mTrailersList;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mTrailerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    String mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/";
    final String mApiKeyParam = "api_key";
    final String mKeyValue = "2b34f0a753ed8e38b7546773dbed2720";
    RequestQueue queue;

    final static String mKeyMovieId = "movie_id";
    final static String mKeyMoviePosterPath = "movie_poster_full_path";
    final static String mKeyMovieUserRating = "movie_user_rating";
    final static String mKeyMovieReleaseDate = "movie_release_date";
    final static String mKeyMoviePlot = "movie_plot";
    final static String mKeyMovieTitle = "movie_title";
    final static String mKeyMovieBackDropPath = "movie_back_drop_path";

    public MovieDetailsFragment() {
    }

    public static MovieDetailsFragment newInstance(String movieId,
                                                   String movieTitle,
                                                   String moviePosterPath,
                                                   String movieUserRating,
                                                   String movieReleaseDate,
                                                   String moviePlot,
                                                   String movieBackDropPath) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putString(mKeyMovieId, movieId);
        args.putString(mKeyMovieTitle, movieTitle);
        args.putString(mKeyMoviePosterPath, moviePosterPath);
        args.putString(mKeyMovieUserRating, movieUserRating);
        args.putString(mKeyMovieReleaseDate, movieReleaseDate);
        args.putString(mKeyMoviePlot, moviePlot);
        args.putString(mKeyMovieBackDropPath, movieBackDropPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovieId = this.getArguments().getString(mKeyMovieId);
            mMovieTitle = this.getArguments().getString(mKeyMovieTitle);
            mMoviePosterPath = this.getArguments().getString(mKeyMoviePosterPath);
            mMovieUserRating = this.getArguments().getString(mKeyMovieUserRating);
            mMovieReleaseDate = this.getArguments().getString(mKeyMovieReleaseDate);
            mMoviePlot = this.getArguments().getString(mKeyMoviePlot);
            mMovieBackDropPath = this.getArguments().getString(mKeyMovieBackDropPath);
        }
        fetchTrailers();
        fetchReviews();
    }

    public void fetchTrailers() {

        String fetchTrailersUrl = mFetchMoviesBaseUrl + mMovieId + "/" + "videos";
        Uri builtUri = Uri.parse(fetchTrailersUrl)
                .buildUpon()
                .appendQueryParameter(mApiKeyParam, mKeyValue)
                .build();
        String url = builtUri.toString();
        Log.d("URL:", url);
        queue = Volley.newRequestQueue(getActivity());
        FetchDataTask fetchMoviesTask = new FetchDataTask();
        VolleyCallBack volleyCallBack = new VolleyCallBack() {
            @Override
            public void returnResponse(String result) {

                getTrailers(result);
                Log.d("Response trailers:", result);
            }
        };
        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
    }

    public void getTrailers(String jsonResult) {
        String trailerKey;
        final String TMDB_RESULTS = "results";
        final String TMDB_TRAILER_KEY = "key";
        try {
            JSONObject trailersJsonObject = new JSONObject(jsonResult);
            JSONArray trailersArray = trailersJsonObject.getJSONArray(TMDB_RESULTS);
            mTrailersList.clear();
            for (int i = 0; i < trailersArray.length(); i++) {
                trailerKey = "https://i.ytimg.com/vi/" + trailersArray.getJSONObject(i).getString(TMDB_TRAILER_KEY) + "/default.jpg";
                mTrailersList.add(trailerKey);
                Log.d("Trailerkey:", mTrailersList.get(i));

            }
            mTrailerAdapter.notifyDataSetChanged();

        } catch (JSONException je) {

        }
    }

    public void fetchReviews() {
        String fetchReviewsUrl = mFetchMoviesBaseUrl + mMovieId + "/" + "reviews";
        Uri builtUri = Uri.parse(fetchReviewsUrl)
                .buildUpon()
                .appendQueryParameter(mApiKeyParam, mKeyValue)
                .build();
        String url = builtUri.toString();
        Log.d("URL:", url);
        FetchDataTask fetchMoviesTask = new FetchDataTask();
        VolleyCallBack volleyCallBack = new VolleyCallBack() {
            @Override
            public void returnResponse(String result) {
//                try {
//                    getMoviesDataFromJson(result);
//                    onPostExecute();
//                } catch (JSONException je) {
//
//                }
                Log.d("Response reviews:", result);
            }
        };
        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        mTrailersList = new ArrayList<String>();
        mTrailerAdapter = new TrailerAdapter();
//        LinearLayout trailerView = (LinearLayout) rootView.findViewById(R.id.trailer_list);
//        trailerView.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.canScrollHorizontally();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mTrailerAdapter);


        TextView movieTitleText = (TextView) rootView.findViewById(R.id.movie_title_text_view);
        movieTitleText.setText(mMovieTitle);

        ImageView moviePosterImage = (ImageView) rootView.findViewById(R.id.movie_poster_image_view);
        Picasso.with(getActivity()).load(mMoviePosterPath).placeholder(R.drawable.placeholder_loading).resize(400, 350).into(moviePosterImage);

        mMovieUserRating = mMovieUserRating + "/10";
        TextView movieRatingText = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        movieRatingText.setText(mMovieUserRating);

        TextView movieOverviewText = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        movieOverviewText.setText(mMoviePlot);

        TextView movieReleaseDateText = (TextView) rootView.findViewById(R.id.movie_releasedate_text_view);
        movieReleaseDateText.setText(mMovieReleaseDate);

        return rootView;
    }

    public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
        Context mContext;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public ImageView mTrailerImageView;

            public ViewHolder(ImageView v) {
                super(v);
                mTrailerImageView = v;
            }
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Picasso.with(getContext()).load(mTrailersList.get(position)).placeholder(R.drawable.placeholder_loading).into(holder.mTrailerImageView);
        }

        @Override
        public int getItemCount() {
            Log.d("Listsize:",String.valueOf(mTrailersList.size()));
            return mTrailersList.size();
        }

        @Override
        public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trailer_image_view, parent, false);
                v.setLayoutParams(new RecyclerView.LayoutParams(700, 500));
            v.setScaleType(ImageView.ScaleType.FIT_XY);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


    }

}