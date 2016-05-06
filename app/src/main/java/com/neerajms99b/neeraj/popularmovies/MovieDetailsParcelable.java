package com.neerajms99b.neeraj.popularmovies;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neeraj on 29/4/16.
 */
public class MovieDetailsParcelable implements Parcelable {
    public String mMovieId;
    public String mMovieTitle;
    public String mMoviePosterFullPath;
    public String mMovieUserRating;
    public String mMovieReleaseDate;
    public String mMoviePlot;
    public String mMovieBackDropPath;

    public MovieDetailsParcelable(String movieId,
                                  String movieTitle,
                                  String moviePosterFullPath,
                                  String movieUserRating,
                                  String movieReleaseDate,
                                  String moviePlot,
                                  String movieBackDropPath) {
        this.mMovieId = movieId;
        this.mMovieTitle = movieTitle;
        this.mMoviePosterFullPath = moviePosterFullPath;
        this.mMovieUserRating = movieUserRating;
        this.mMovieReleaseDate = movieReleaseDate;
        this.mMoviePlot = moviePlot;
        this.mMovieBackDropPath = movieBackDropPath;
    }

    private MovieDetailsParcelable(Parcel in) {
        mMovieId = in.readString();
        mMovieTitle = in.readString();
        mMoviePosterFullPath = in.readString();
        mMovieUserRating = in.readString();
        mMovieReleaseDate = in.readString();
        mMoviePlot = in.readString();
        mMovieBackDropPath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMovieId);
        dest.writeString(mMovieTitle);
        dest.writeString(mMoviePosterFullPath);
        dest.writeString(mMovieUserRating);
        dest.writeString(mMovieReleaseDate);
        dest.writeString(mMoviePlot);
        dest.writeString(mMovieBackDropPath);
    }

    public static final Parcelable.Creator<MovieDetailsParcelable> CREATOR
            = new Parcelable.Creator<MovieDetailsParcelable>() {
        public MovieDetailsParcelable createFromParcel(Parcel in) {
            return new MovieDetailsParcelable(in);
        }

        public MovieDetailsParcelable[] newArray(int size) {
            return new MovieDetailsParcelable[size];
        }
    };
}
