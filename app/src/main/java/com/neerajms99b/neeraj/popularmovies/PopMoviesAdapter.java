package com.neerajms99b.neeraj.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by neeraj on 29/4/16.
 */
public class PopMoviesAdapter extends ArrayAdapter<MovieDetailsParcelable> {
    Context mContext;

    PopMoviesAdapter(Context context) {
        super(context, 0);
        mContext = context;
    }

    @Override
    public int getCount() {
        return MainActivityFragment.getMovieDetailsArrayList().size();
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
            if (new MainActivity().isTwoPane()) {
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
            } else {
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));
            }
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView = (ImageView) convertView;
        }
        MovieDetailsParcelable tempMovieObject = MainActivityFragment.getMovieDetailsArrayList().get(position);
        Picasso.with(mContext)
                .load(tempMovieObject.mMoviePosterFullPath)
                .placeholder(R.drawable.placeholder_loading)
                .into(imageView);
        return imageView;
    }
}
