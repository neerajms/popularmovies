package com.neerajms99b.neeraj.popularmovies;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment {
    String moviePosterPath;
    String movieUserRating;
    String movieReleaseDate;
    String moviePlot;
    String movieTitle;
    String movieBackDropPath;

    final static String KEY_MOVIE_POSTER_PATH = "movie_poster_full_path";
    final static String KEY_MOVIE_USER_RATING = "movie_user_rating";
    final static String KEY_MOVIE_RELEASE_DATE = "movie_release_date";
    final static String KEY_MOVIE_PLOT = "movie_plot";
    final static String KEY_MOVIE_TITLE = "movie_title";
    final static String KEY_MOVIE_BACK_DROP_PATH = "movie_back_drop_path";

    public MovieDetailsFragment() {
    }

    public static MovieDetailsFragment newInstance(String movieTitle,
                                                   String moviePosterPath,
                                                   String movieUserRating,
                                                   String movieReleaseDate,
                                                   String moviePlot,
                                                   String movieBackDropPath) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MOVIE_TITLE, movieTitle);
        args.putString(KEY_MOVIE_POSTER_PATH, moviePosterPath);
        args.putString(KEY_MOVIE_USER_RATING, movieUserRating);
        args.putString(KEY_MOVIE_RELEASE_DATE, movieReleaseDate);
        args.putString(KEY_MOVIE_PLOT, moviePlot);
        args.putString(KEY_MOVIE_BACK_DROP_PATH,movieBackDropPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            movieTitle = this.getArguments().getString(KEY_MOVIE_TITLE);
            moviePosterPath = this.getArguments().getString(KEY_MOVIE_POSTER_PATH);
            movieUserRating = this.getArguments().getString(KEY_MOVIE_USER_RATING);
            movieReleaseDate = this.getArguments().getString(KEY_MOVIE_RELEASE_DATE);
            moviePlot = this.getArguments().getString(KEY_MOVIE_PLOT);
            movieBackDropPath = this.getArguments().getString(KEY_MOVIE_BACK_DROP_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        TextView movieTitleText = (TextView) rootView.findViewById(R.id.movie_title_text_view);
        movieTitleText.setText(movieTitle);

        ImageView moviePosterImage = (ImageView) rootView.findViewById(R.id.movie_poster_image_view);
        Picasso.with(getActivity()).load(moviePosterPath).resize(400, 350).into(moviePosterImage);

        movieUserRating = movieUserRating + "/10";
        TextView movieRatingText = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        movieRatingText.setText(movieUserRating);

        TextView movieOverviewText = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        movieOverviewText.setText(moviePlot);

        TextView movieReleaseDateText = (TextView) rootView.findViewById(R.id.movie_releasedate_text_view);
        movieReleaseDateText.setText(movieReleaseDate);

        return rootView;
    }
}