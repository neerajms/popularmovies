package com.neerajms99b.neeraj.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ReviewDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String content = intent.getExtras().getString("review_content");

        TextView reviewTextView = (TextView)findViewById(R.id.review_text_view);
        reviewTextView.setText(content);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
