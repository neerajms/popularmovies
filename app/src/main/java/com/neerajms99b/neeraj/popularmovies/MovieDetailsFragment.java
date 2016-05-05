package com.neerajms99b.neeraj.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Base64;
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
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    String mBackDropBitmap;

    ShareActionProvider mShareActionProvider;

    static String mShareUrl;
    public static Bitmap mBitmapPoster;


    public MainActivity mCallBack;

    private ArrayList<String> mTrailersList;
    private String trailerKey;
    ArrayList<ReviewDetails> mReviewsList;
    private RecyclerView mTrailersRecyclerView;
    private RecyclerView.Adapter mTrailerAdapter;
    private RecyclerView.LayoutManager mTrailersLayoutManager;

    private RecyclerView mReviewsRecyclerView;
    private RecyclerView.Adapter mReviewsAdapter;
    private RecyclerView.LayoutManager mReviewsLayoutManager;

    String mFetchMoviesBaseUrl = "https://api.themoviedb.org/3/movie/";
    final String mApiKeyParam = "api_key";
    final String mKeyValue = "2b34f0a753ed8e38b7546773dbed2720";
    RequestQueue queue;

    final static String mKeyTrailerList = "trailer_list";
    final static String mKeyMovieId = "movie_id";
    final static String mKeyMoviePosterPath = "movie_poster_full_path";
    final static String mKeyMovieUserRating = "movie_user_rating";
    final static String mKeyMovieReleaseDate = "movie_release_date";
    final static String mKeyMoviePlot = "movie_plot";
    final static String mKeyMovieTitle = "movie_title";
    final static String mKeyMovieBackDropPath = "movie_back_drop_path";

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
//        Log.d("TrailerKey MovDetFrag::",trailersKey);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
//        args.putString(mKeyTrailerList, trailersKey);
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

    public interface BitmapInterface {
        public void getPosterBitmap(Bitmap poster);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (getArguments() != null) {
            trailerKey = this.getArguments().getString(mKeyTrailerList);
            mMovieId = this.getArguments().getString(mKeyMovieId);
            mMovieTitle = this.getArguments().getString(mKeyMovieTitle);
            mMoviePosterPath = this.getArguments().getString(mKeyMoviePosterPath);
            mMovieUserRating = this.getArguments().getString(mKeyMovieUserRating);
            mMovieReleaseDate = this.getArguments().getString(mKeyMovieReleaseDate);
            mMoviePlot = this.getArguments().getString(mKeyMoviePlot);
            mMovieBackDropPath = this.getArguments().getString(mKeyMovieBackDropPath);
        }

        mTrailerAdapter = new TrailerAdapter(getActivity());
        mReviewsAdapter = new ReviewsAdapter();
//            mCallBack = (MainActivity) getActivity();
//

        fetchTrailers();
        fetchReviews();
        setHasOptionsMenu(true);
    }


    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {


        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_details, menu);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                "trailerShared", Context.MODE_PRIVATE);

        trailerKey = sharedPref.getString(mMovieId,"trailerkey");
//        Log.d("TraileerKey:::::::::",trailerKey);
//        Log.d("TrailerKey OncrtOP::",trailerKey);
        mShareUrl = "http://www.youtube.com/watch?v=" + trailerKey;
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
//            Log.d("Share URL shr:",mShareUrl);

        } else {
            Log.e("Error", "Intent not found");
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//
//        return super.onOptionsItemSelected(item);
//    }

    public static Intent createShareTrailerIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
//        Log.d("Share URL:",mShareUrl);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareUrl);
        return shareIntent;
    }

//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//            setHasOptionsMenu(true);
//        }
//
//
//    }

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

        }
        if (!mTrailersList.isEmpty()) {
            mShareUrl = "http://www.youtube.com/watch?v=" + mTrailersList.get(0);
        } else {
            mShareUrl = "Not found";
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
            for (int i = 0; i < reviewsJsonArray.length(); i++) {
                author = reviewsJsonArray.getJSONObject(i).getString(TMDB_REVIEW_AUTHOR);
                content = reviewsJsonArray.getJSONObject(i).getString(TMDB_REVIEW_CONTENT);
                ReviewDetails reviewDetailsObject = new ReviewDetails(author, content);

                mReviewsList.add(reviewDetailsObject);

            }
            mReviewsAdapter.notifyDataSetChanged();

        } catch (JSONException je) {

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

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


//        LinearLayout trailerView = (LinearLayout) rootView.findViewById(R.id.trailer_list);
//        trailerView.
        mTrailersRecyclerView = (RecyclerView) rootView.findViewById(R.id.trailer_recycler_view);
        mTrailersRecyclerView.setHasFixedSize(true);
        mTrailersLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mTrailersLayoutManager.canScrollHorizontally();
        mTrailersRecyclerView.setLayoutManager(mTrailersLayoutManager);
        mTrailersRecyclerView.setAdapter(mTrailerAdapter);

//        mTrailerAdapter.notifyDataSetChanged();
//mTrailerAdapter
        mReviewsRecyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
        mReviewsRecyclerView.setHasFixedSize(true);
        mReviewsLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mReviewsLayoutManager.canScrollVertically();
        mReviewsRecyclerView.setLayoutManager(mReviewsLayoutManager);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        final ImageView backDropImageView = (ImageView) rootView.findViewById(R.id.back_drop_image);

//        ImageButton favoriteImageButton = (ImageButton)rootView.findViewById(R.id.favorite_button);
        String url = String.valueOf(MoviesContentProvider.uri) + "/" + mMovieId;
        Uri queryUri = Uri.parse(url);
        Cursor c = getContext().getContentResolver().query(queryUri, null, null, null, null);
//        Log.d("CUrsor value:", String.valueOf(c));

        if (c.moveToFirst()) {
            favoriteImageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
            favoriteImageButton.setTag("R.drawable.favorite_clicked");
//            Log.d("Cursor status:", "Found");
        } else {
            favoriteImageButton.setImageResource(R.drawable.ic_action_favourite);
            favoriteImageButton.setTag("R.drawable.favorite");
        }

        TextView movieTitleText = (TextView) rootView.findViewById(R.id.movie_title_text_view);
        movieTitleText.setText(mMovieTitle);

        final ImageView moviePosterImage = (ImageView) rootView.findViewById(R.id.movie_poster_image_view);

        if (MainActivityFragment.noNetwork) {
            FileInputStream inputStream;
            try {
                File f = new File(mMoviePosterPath, mMovieId + ".png");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
//                ImageView img=(ImageView)findViewById(R.id.temp_image);
//                img.setImageBitmap(b);
                moviePosterImage.setImageBitmap(b);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(getActivity()).load(mMoviePosterPath).placeholder(R.drawable.placeholder_loading).resize(400, 350).into(moviePosterImage);
        }

        mMovieUserRating = mMovieUserRating + "/10";
        TextView movieRatingText = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        movieRatingText.setText(mMovieUserRating);

        TextView movieOverviewText = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        movieOverviewText.setText(mMoviePlot);

        TextView movieReleaseDateText = (TextView) rootView.findViewById(R.id.movie_releasedate_text_view);
        movieReleaseDateText.setText(mMovieReleaseDate);

//        favoriteImageButton.setOnClickListener(new View.OnClickListener() {
//            Bitmap bitmapBackDrop;
//
//            @Override
//            public void onClick(View v) {
//                String posterPath = getContext().getFilesDir().getAbsolutePath();
//                String backDropPath = getContext().getFilesDir().getAbsolutePath();
////                ImageButton imageButton = (ImageButton)v.findViewById(R.id.favorite_button);
//
//                if (favoriteImageButton.getTag().equals("R.drawable.favorite")) {
//                    favoriteImageButton.setImageResource(R.drawable.ic_action_favorite_clicked);
//                    favoriteImageButton.setTag("R.drawable.favorite_clicked");
//                    Toast.makeText(getActivity(), "Movie favorited", Toast.LENGTH_SHORT).show();
//                    MainActivityFragment.mDataSetChanged = false;
//
//
////                    ImageView posterImageView = (ImageView)v.findViewById(R.id.movie_poster_image_view);
//                    BitmapDrawable posterBitmapDrawable = (BitmapDrawable) moviePosterImage.getDrawable();
//                    Bitmap bitmapPoster = posterBitmapDrawable.getBitmap();
//
//                    if (!mBackDropBitmap.equals("nobitmap")) {
//                        try {
//                            byte[] encodeByte = Base64.decode(mBackDropBitmap, Base64.DEFAULT);
//                            bitmapBackDrop = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
//
//                        } catch (Exception e) {
//                            e.getMessage();
//
//                        }
//                        saveImage(bitmapBackDrop, mMovieId + "back");
//                    }
//                    saveImage(bitmapPoster, mMovieId);
//
////            FileInputStream inputStream;
////            try {
////                File f=new File(backDropPath, mMovieId+"back.png");
////                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
////                ImageView img=(ImageView)findViewById(R.id.temp_image);
////                img.setImageBitmap(b);
////
////            }
////            catch (FileNotFoundException e)
////            {
////                e.printStackTrace();
////            }
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put(MoviesContentProvider.KEY_ID, mMovieId);
//                    contentValues.put(MoviesContentProvider.KEY_MOVIE_TITLE, mMovieTitle);
//                    contentValues.put(MoviesContentProvider.KEY_MOVIE_BACKDROP, backDropPath);
//                    contentValues.put(MoviesContentProvider.KEY_MOVIE_OVERVIEW, mMoviePlot);
//                    contentValues.put(MoviesContentProvider.KEY_MOVIE_POSTER, posterPath);
//                    contentValues.put(MoviesContentProvider.KEY_MOVIE_RELEASE_DATE, mMovieReleaseDate);
//                    contentValues.put(MoviesContentProvider.KEY_MOVIE_USER_RATING, mMovieUserRating);
//                    Uri uri = getContext().getContentResolver().insert(MoviesContentProvider.uri, contentValues);
////            Log.d("saved",String.valueOf(uri));
//
//                } else if (favoriteImageButton.getTag().equals("R.drawable.favorite_clicked")) {
//
//                    favoriteImageButton.setImageResource(R.drawable.ic_action_favourite);
//                    favoriteImageButton.setTag("R.drawable.favorite");
//                    Toast.makeText(getContext(), "Movie unfavorited", Toast.LENGTH_SHORT).show();
//                    String url = String.valueOf(MoviesContentProvider.uri) + "/" + mMovieId;
//                    Uri queryUri = Uri.parse(url);
//                    Log.d("url to delete:::::", String.valueOf(queryUri));
//                    int i = getContext().getContentResolver().delete(queryUri, null, null);
//                    Log.d("Value of i", String.valueOf(i));
//                    if (i > 0) {
//                        MainActivityFragment.mDataSetChanged = true;
//                        Log.d("Poster path::::::", posterPath);
//                        File posterFile = new File(posterPath, mMovieId + ".png");
//                        boolean deletedPoster = posterFile.delete();
//                        if (!mBackDropBitmap.equals("nobitmap")) {
//                            File backDropFile = new File(backDropPath, mMovieId + "back.png");
//                            boolean deletedBackDrop = backDropFile.delete();
//                            if (deletedBackDrop) {
//                                Log.d("Backdrop status:", "deleted");
//                            }
//                        }
//                        if (deletedPoster) {
//                            Log.d("POSTER status:", "deleted");
//                        }
//
//                    }
//                }
//
//            }
//        });

        return rootView;
    }

    public void saveImage(Bitmap bitmap, String fileName) {
//        ContextWrapper cw = new ContextWrapper(getApplicationContext());
//        File myDir = cw.getDir("movies", Context.MODE_PRIVATE);
//        if (!myDir.exists()) {
//            myDir.mkdir();
//        }
        File image = new File(getContext().getFilesDir(), fileName + ".png");
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
//            Log.d("File Operation:", mMovieId + ".png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //public static Bitmap getPoster(){
//    if(mCallBack.isTwoPane()) {
//        BitmapDrawable posterBitmapDrawable = (BitmapDrawable) mMoviePosterImage.getDrawable();
//        mBitmapPoster = posterBitmapDrawable.getBitmap();
////        mCallBack.getPosterBitmap(mBitmapPoster);
//    }
//    return mBitmapPoster;
//}
    public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
        Context mContext;


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // each data item is just a string in this case
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
            Picasso.with(getContext()).load("https://i.ytimg.com/vi/" + mTrailersList.get(position) + "/hqdefault.jpg").placeholder(R.drawable.placeholder_loading).into(holder.mTrailerImageView);

        }

        @Override
        public int getItemCount() {
            return mTrailersList.size();
        }

        @Override
        public TrailerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trailer_image_view, parent, false);
            v.setLayoutParams(new RecyclerView.LayoutParams(700, 500));
            v.setPadding(32, 32, 0, 32);
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
        Context mContext;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // each data item is just a string in this case
            public LinearLayout mReviewsLayout;
            public TextView mAuthorTextView;
            public TextView mContentTextView;

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReviewDisplayActivity.class);
                intent.putExtra("review_content", mReviewsList.get(getPosition()).mContent);
                startActivity(intent);
            }

            public ViewHolder(LinearLayout v, TextView authorTextView, TextView contentTextView) {
                super(v);
//                contentTextView.setClickable(true);
//               contentTextView.setOnClickListener(this);
                mReviewsLayout = v;
                mAuthorTextView = authorTextView;
                mContentTextView = contentTextView;

            }
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            Picasso.with(getContext()).load(mTrailersList.get(position)).placeholder(R.drawable.placeholder_loading).into(holder.mTrailerImageView);
            holder.mAuthorTextView.setText("Review by " + mReviewsList.get(position).mAuthor);

            holder.mContentTextView.setText(mReviewsList.get(position).mContent);

        }

        @Override
        public int getItemCount() {
            return mReviewsList.size();
        }

        @Override
        public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_item, parent, false);
            v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView author = (TextView) v.findViewById(R.id.author_text_view);
            TextView content = (TextView) v.findViewById(R.id.content_text_view);
//            content.setMaxLines(10);
            ViewHolder vh = new ViewHolder(v, author, content);
            return vh;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


    }

}