package com.example.android.booklisting;


public class Book {
    private String mTitle;
    private String[] mAuthor;
    private String mSummary;
    private String mUrl;

    public Book(String title, String[] authors, String summary, String url) {
        mTitle= title;
        mAuthor = authors;
        mSummary = summary;
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public String[] getAuthor() {
        return mAuthor;
    }

    public String generateStringOfAuthor() {
        String result = "";

        for (int i=0; i<mAuthor.length; i++) {
            if (i == mAuthor.length-1) {
                result += mAuthor[i];
            } else {
                result += mAuthor[i] + ", ";
            }
        }
        return result;
    }

    public String getSummary() {
        return mSummary;
    }
    public String getUrl() {
        return mUrl;
    }
}
