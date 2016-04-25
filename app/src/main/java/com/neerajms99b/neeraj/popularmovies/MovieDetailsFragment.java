package com.neerajms99b.neeraj.popularmovies;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailsFragment extends Fragment {
    String mMoviePosterPath;
    String mMovieUserRating;
    String mMovieReleaseDate;
    String mMoviePlot;
    String mMovieTitle;
    String mMovieBackDropPath;

    final static String mKeyMoviePosterPath = "movie_poster_full_path";
    final static String mKeyMovieUserRating = "movie_user_rating";
    final static String mKeyMovieReleaseDate = "movie_release_date";
    final static String mKeyMoviePlot = "movie_plot";
    final static String mKeyMovieTitle = "movie_title";
    final static String mKeyMovieBackDropPath = "movie_back_drop_path";

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
        args.putString(mKeyMovieTitle, movieTitle);
        args.putString(mKeyMoviePosterPath, moviePosterPath);
        args.putString(mKeyMovieUserRating, movieUserRating);
        args.putString(mKeyMovieReleaseDate, movieReleaseDate);
        args.putString(mKeyMoviePlot, moviePlot);
        args.putString(mKeyMovieBackDropPath,movieBackDropPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMovieTitle = this.getArguments().getString(mKeyMovieTitle);
            mMoviePosterPath = this.getArguments().getString(mKeyMoviePosterPath);
            mMovieUserRating = this.getArguments().getString(mKeyMovieUserRating);
            mMovieReleaseDate = this.getArguments().getString(mKeyMovieReleaseDate);
            mMoviePlot = this.getArguments().getString(mKeyMoviePlot);
            mMovieBackDropPath = this.getArguments().getString(mKeyMovieBackDropPath);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);

        TextView movieTitleText = (TextView) rootView.findViewById(R.id.movie_title_text_view);
        movieTitleText.setText(mMovieTitle);

        ImageView moviePosterImage = (ImageView) rootView.findViewById(R.id.movie_poster_image_view);
        Picasso.with(getActivity()).load(mMoviePosterPath).resize(400, 350).into(moviePosterImage);

        mMovieUserRating = mMovieUserRating + "/10";
        TextView movieRatingText = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
        movieRatingText.setText(mMovieUserRating);

        TextView movieOverviewText = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
        movieOverviewText.setText(mMoviePlot);

        TextView movieReleaseDateText = (TextView) rootView.findViewById(R.id.movie_releasedate_text_view);
        movieReleaseDateText.setText(mMovieReleaseDate);

        return rootView;
    }
}