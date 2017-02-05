package com.example.android.booklisting;


import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class QueryUtils {

    static final String LOG_TAG = QueryUtils.class.getSimpleName();

    public QueryUtils() {
    }

    // Returns new URL object from the given String URL
    private static URL createURL(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem with the URL", e);
        }
        return url;
    }


}
