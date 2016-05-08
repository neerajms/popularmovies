package com.neerajms99b.neeraj.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by neeraj on 8/5/16.
 */
public class ReviewDetailsParcelable implements Parcelable {
    String mAuthor;
    String mContent;

    public ReviewDetailsParcelable(String author, String content) {
        this.mAuthor = author;
        this.mContent = content;
    }

    protected ReviewDetailsParcelable(Parcel in) {
        mAuthor = in.readString();
        mContent = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthor);
        dest.writeString(mContent);
    }

    public static final Creator<ReviewDetailsParcelable> CREATOR = new Creator<ReviewDetailsParcelable>() {
        @Override
        public ReviewDetailsParcelable createFromParcel(Parcel in) {
            return new ReviewDetailsParcelable(in);
        }

        @Override
        public ReviewDetailsParcelable[] newArray(int size) {
            return new ReviewDetailsParcelable[size];
        }
    };
}