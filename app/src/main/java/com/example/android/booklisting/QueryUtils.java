package com.example.android.booklisting;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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

    // Using HttpsUrlConnection to Fetch Data
    private static String makeHttpRequest(URL url) throws IOException{

        String jsonResult = "";

        if (url == null) {
            return jsonResult;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(100000);
            urlConnection.setConnectTimeout(15000);
            // Set HTTP method to GET
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResult = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON result", e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResult;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //JSON Response Parsing:Return a list of objects that has been built up from parsing the given JSON response result
    private static List<Book> extractInfoFromJson(String bookJSON) {
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        List<Book> books = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(bookJSON);
            JSONArray items = jsonObj.getJSONArray("items");

            for (int i=0; i<items.length(); i++) {
                JSONObject currentBook = items.getJSONObject(i);
                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");

                String title = volumeInfo. getString("title");

                String authors =volumeInfo.getString("authors");

                String description = volumeInfo.getString("description");

                String infoLink = volumeInfo.getString("infoLink");

                Book book = new Book(title, authors, description, infoLink);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing earthquake JSON result", e);
        }
        return books;
    }

    // fetchBookData() helper method that ties all the steps togheter
    public static List<Book> fetchBookData(String requestUrl) {
        URL url = createURL(requestUrl);

        String jsonResponse = null;

        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem with making the HTTP request", e);
        }

        List<Book> books = extractInfoFromJson(jsonResponse);

        Log.i(LOG_TAG, "fetchBookData is initiated");

        return books;
    }
}
