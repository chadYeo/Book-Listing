package com.example.android.booklisting;


public class Book {
    private String mTitle;
    private String mAuthor;
    private String mSummary;
    private String mUrl;

    public Book(String title, String authors, String summary, String url) {
        mTitle= title;
        mAuthor = authors;
        mSummary = summary;
        mUrl = url;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public String getmSummary() {
        return mSummary;
    }
    public String getmUrl() {
        return mUrl;
    }
}
