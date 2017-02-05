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

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getSummary() {
        return mSummary;
    }
    public String getUrl() {
        return mUrl;
    }
}
