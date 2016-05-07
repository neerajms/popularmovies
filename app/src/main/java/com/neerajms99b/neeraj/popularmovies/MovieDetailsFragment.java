package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment {
    private String mMovieId;
    private String mMoviePosterPath;
    private String mMovieUserRating;
    private String mMovieReleaseDate;
    private String mMoviePlot;
    private String mMovieTitle;
    private String mMovieBackDropPath;

    private static String mShareUrl;

    private MovieDetailsParcelable movieDetailsParcelable;

    private ArrayList<String> mTrailersList;
    private String trailerKey;
    private RecyclerView mTrailersRecyclerView;
    private RecyclerView.Adapter mTrailerAdapter;
    private RecyclerView.LayoutManager mTrailersLayoutManager;

    private ArrayList<ReviewDetails> mReviewsList;
    private RecyclerView mReviewsRecyclerView;
    private RecyclerView.Adapter mReviewsAdapter;
    private RecyclerView.LayoutManager mReviewsLayoutManager;

    private String mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/";
    private final String mApiKeyParam = "api_key";
    private final String mKeyValue = "2b34f0a753ed8e38b7546773dbed2720";
    private RequestQueue queue;

    private final static String mKeyTrailerList = "trailer_list";
    private final static String mKeyMovieId = "movie_id";
    private final static String mKeyMoviePosterPath = "movie_poster_full_path";
    private final static String mKeyMovieUserRating = "movie_user_rating";
    private final static String mKeyMovieReleaseDate = "movie_release_date";
    private final static String mKeyMoviePlot = "movie_plot";
    private final static String mKeyMovieTitle = "movie_title";
    private final static String mKeyMovieBackDropPath = "movie_back_drop_path";

    public MovieDetailsFragment() {
    }

    public static MovieDetailsFragment newInstance(
            String movieId,
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
        if (savedInstanceState != null && savedInstanceState.containsKey("parcel")) {
            movieDetailsParcelable = savedInstanceState.getParcelable("parcel");
            mMovieId = movieDetailsParcelable.mMovieId;
            mMovieTitle = movieDetailsParcelable.mMovieTitle;
            mMoviePlot = movieDetailsParcelable.mMoviePlot;
            mMoviePosterPath = movieDetailsParcelable.mMoviePosterFullPath;
            mMovieReleaseDate = movieDetailsParcelable.mMovieReleaseDate;
            mMovieUserRating = movieDetailsParcelable.mMovieUserRating;
            mMovieBackDropPath = movieDetailsParcelable.mMovieBackDropPath;

        } else if (getArguments() != null) {
            trailerKey = this.getArguments().getString(mKeyTrailerList);
            mMovieId = this.getArguments().getString(mKeyMovieId);
            mMovieTitle = this.getArguments().getString(mKeyMovieTitle);
            mMoviePosterPath = this.getArguments().getString(mKeyMoviePosterPath);
            mMovieUserRating = this.getArguments().getString(mKeyMovieUserRating);
            mMovieReleaseDate = this.getArguments().getString(mKeyMovieReleaseDate);
            mMoviePlot = this.getArguments().getString(mKeyMoviePlot);
            mMovieBackDropPath = this.getArguments().getString(mKeyMovieBackDropPath);

            movieDetailsParcelable = new MovieDetailsParcelable(
                    mMovieId,
                    mMovieTitle,
                    mMoviePosterPath,
                    mMovieUserRating,
                    mMovieReleaseDate,
                    mMoviePlot,
                    mMovieBackDropPath
            );
        }
        mTrailerAdapter = new TrailerAdapter(getActivity());
        mReviewsAdapter = new ReviewsAdapter();
        fetchTrailers();
        fetchReviews();
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("parcel", movieDetailsParcelable);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_details, menu);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                "trailerShared", Context.MODE_PRIVATE);
        trailerKey = sharedPref.getString(mMovieId, "trailerkey");
        mShareUrl = "http://www.youtube.com/watch?v=" + trailerKey;

        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        } else {
            Log.e("Error", "Intent not found");
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    public static Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareUrl);
        return shareIntent;
    }

    public void fetchTrailers() {
        String fetchTrailersUrl = mFetchMoviesBaseUrl + mMovieId + "/" + "videos";
        Uri builtUri = Uri.parse(fetchTrailersUrl)
                .buildUpon()
                .appendQueryParameter(mApiKeyParam, mKeyValue)
                .build();
        String url = builtUri.toString();

        queue = Volley.newRequestQueue(getActivity());
        FetchDataTask fetchMoviesTask = new FetchDataTask();
        VolleyCallBack volleyCallBack = new VolleyCallBack() {
            @Override
            public void returnResponse(String result) {
                getTrailersFromJson(result);
            }
        };
        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
    }

    public void getTrailersFromJson(String jsonResult) {
        String trailerKey;
        final String TMDB_RESULTS = "results";
        final String TMDB_TRAILER_KEY = "key";
        try {
            JSONObject trailersJsonObject = new JSONObject(jsonResult);
            JSONArray trailersArray = trailersJsonObject.getJSONArray(TMDB_RESULTS);
            mTrailersList.clear();
            for (int i = 0; i < trailersArray.length(); i++) {
                trailerKey = trailersArray.getJSONObject(i).getString(TMDB_TRAILER_KEY);
                mTrailersList.add(trailerKey);
            }
            mTrailerAdapter.notifyDataSetChanged();
        } catch (JSONException je) {
            Log.d("Error:", "Could not extract trailers from JSON");
        }
    }

    public void fetchReviews() {
        String fetchReviewsUrl = mFetchMoviesBaseUrl + mMovieId + "/" + "reviews";
        Uri builtUri = Uri.parse(fetchReviewsUrl)
                .buildUpon()
                .appendQueryParameter(mApiKeyParam, mKeyValue)
                .build();
        String url = builtUri.toString();
        queue = Volley.newRequestQueue(getActivity());
        FetchDataTask fetchMoviesTask = new FetchDataTask();
        VolleyCallBack volleyCallBack = new VolleyCallBack() {
            @Override
            public void returnResponse(String result) {
                Log.d("JSON RESULT::", result);
                getReviewsFromJson(result);
            }
        };
        fetchMoviesTask.executeThread(url, queue, volleyCallBack);
    }

    public void getReviewsFromJson(String jsonResult) {
        String author;
        String content;
        final String TMDB_RESULTS = "results";
        final String TMDB_REVIEW_AUTHOR = "author";
        final String TMDB_REVIEW_CONTENT = "content";
        try {
            JSONObject reviewsJsonObject = new JSONObject(jsonResult);
            JSONArray reviewsJsonArray = reviewsJsonObject.getJSONArray(TMDB_RESULTS);
            mReviewsList.clear();
            if (reviewsJsonArray.length() == 0) {
                author = "No review found";
                content = "";
                ReviewDetails noReviewFound = new ReviewDetails(author, content);
                mReviewsList.add(noReviewFound);
            }
            for (int i = 0; i < reviewsJsonArray.length(); i++) {
                author = "Review by " + reviewsJsonArray.getJSONObject(i).getString(TMDB_REVIEW_AUTHOR);
                content = reviewsJsonArray.getJSONObject(i).getString(TMDB_REVIEW_CONTENT);
                ReviewDetails reviewDetailsObject = new ReviewDetails(author, content);
                mReviewsList.add(reviewDetailsObject);
            }
            mReviewsAdapter.notifyDataSetChanged();

        } catch (JSONException je) {
            Log.d("Error:", "Could not extract reviews from JSON");
        }
    }

    public class ReviewDetails {
        String mAuthor;
        String mContent;

        public ReviewDetails(String author, String content) {
            this.mAuthor = author;
            this.mContent = content;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        final ImageButton favoriteImageButton = (ImageButton) rootView.findViewById(R.id.favorite_button);

        mTrailersList = new ArrayList<String>();
        mReviewsList = new ArrayList<ReviewDetails>();

        mTrailersRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_recycler_view);
        mTrailersRecyclerView.setHasFixedSize(true);
        mTrailersLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mTrailersLayoutManager.canScrollHorizontally();
        mTrailersRecyclerView.setLayoutManager(mTrailersLayoutManager);
        mTrailersRecyclerView.setAdapter(mTrailerAdapter);

        mReviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
        mReviewsRecyclerView.setHasFixedSize(true);
        mReviewsLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mReviewsLayoutManager.canScrollVertically();
        mReviewsRecyclerView.setLayoutManager(mReviewsLayoutManager);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        String url = String.valueOf(MoviesContentProvider.uri) + "/" + mMovieId;
        Uri queryUri = Uri.parse(url);
        Cursor c = getContext().getContentResolver().query(queryUri, null, null, null, null);
        if (c.moveToFirst()) {
            favoriteImageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
            favoriteImageButton.setTag("R.drawable.favorite_clicked");
        } else {
            favoriteImageButton.setImageResource(R.drawable.ic_action_favorite);
            favoriteImageButton.setTag("R.drawable.favorite");
        }

        TextView movieTitleText = (TextView) rootView.findViewById(R.id.movie_title_text_view);
        movieTitleText.setText(mMovieTitle);

        final ImageView moviePosterImage = (ImageView) rootView.findViewById(R.id.movie_poster_image_view);

        if (MainActivityFragment.offline) {
            try {
                File f = new File(mMoviePosterPath, mMovieId + ".png");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                moviePosterImage.setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(getActivity())
                    .load(mMoviePosterPath).placeholder(R.drawable.placeholder_loading)
                    .resize(400, 350)
                    .into(moviePosterImage);
        }

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

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ImageView mTrailerImageView;

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + mTrailersList.get(getPosition())));
                startActivity(intent);
            }

            public ViewHolder(ImageView v) {
                super(v);
                v.setClickable(true);
                v.setOnClickListener(this);
                mTrailerImageView = v;
            }
        }

        public TrailerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Picasso.with(getContext())
                    .load("https://i.ytimg.com/vi/" + mTrailersList.get(position) + "/hqdefault.jpg")
                    .placeholder(R.drawable.placeholder_loading)
                    .into(holder.mTrailerImageView);
        }

        @Override
        public int getItemCount() {
            return mTrailersList.size();
        }

        @Override
        public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            float density = getContext().getResources().getDisplayMetrics().density;
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trailer_image_view, parent, false);
            v.setLayoutParams(new RecyclerView.LayoutParams(260 * (int) density, 220 * (int) density));
            v.setPadding(8 * (int) density, 8 * (int) density, 0, 8 * (int) density);
            v.setScaleType(ImageView.ScaleType.FIT_XY);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout mReviewsLayout;
            public TextView mAuthorTextView;
            public TextView mContentTextView;

            public ViewHolder(LinearLayout v, TextView authorTextView, TextView contentTextView) {
                super(v);
                mReviewsLayout = v;
                mAuthorTextView = authorTextView;
                mContentTextView = contentTextView;
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mAuthorTextView.setText(mReviewsList.get(position).mAuthor);
            if (!mReviewsList.get(position).mAuthor.equals("No review found")) {
                holder.mContentTextView.setText(mReviewsList.get(position).mContent);
            }
        }

        @Override
        public int getItemCount() {
            return mReviewsList.size();
        }

        @Override
        public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_item, parent, false);
            v.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView author = (TextView) v.findViewById(R.id.author_text_view);
            TextView content = (TextView) v.findViewById(R.id.content_text_view);
            ViewHolder vh = new ViewHolder(v, author, content);
            return vh;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}