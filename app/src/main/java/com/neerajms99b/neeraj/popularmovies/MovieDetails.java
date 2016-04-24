package com.neerajms99b.neeraj.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MovieDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView imageView = (ImageView)findViewById(R.id.app_bar_image);
        Intent intent = getIntent();
        final String movieTitle = intent.getExtras().getString("movieTitle");
        String moviePosterPath = intent.getExtras().getString("moviePosterFullPath");
        String movieUserRating = intent.getExtras().getString("movieUserRating");
        String movieReleaseDate = intent.getExtras().getString("movieReleaseDate");
        String moviePlot = intent.getExtras().getString("moviePlot");
        String movieBackDropPath = intent.getExtras().getString("movieBackDropPath");
        Picasso.with(this).load(movieBackDropPath).into(imageView);
        setTitle(movieTitle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container,
                        MovieDetailsFragment.newInstance(movieTitle,
                                moviePosterPath,
                                movieUserRating,
                                movieReleaseDate,
                                moviePlot,
                                movieBackDropPath), "MovieDetailsFragment").commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}