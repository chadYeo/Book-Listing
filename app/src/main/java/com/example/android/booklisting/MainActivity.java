package com.example.android.booklisting;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private ListView mBookListView;
    private BookAdapter mAdapter;
    private EditText mEditText;
    private Button mSearchButton;
    private TextView mEmptyTextView;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String GOOGLE_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.editText);
        mBookListView = (ListView) findViewById(R.id.listView);

        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Search button method is applied below upon click:
        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookAsyncTask task = new BookAsyncTask();
                Log.i(LOG_TAG, "Search Button is clicked");
                task.execute();
            }
        });

        // Book ListView and setting Adapter below:
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book currentBook = mAdapter.getItem(position);

                Uri bookUri = Uri.parse(currentBook.getUrl());

                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);

                startActivity(websiteIntent);
            }
        });

    }

    /**
     * AsyncTask
     */
    public class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {

        private String searchInput = mEditText.getText().toString();

        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {

            if (searchInput.length() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getResources()
                                .getString(R.string.enter_search_keyword), Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

            searchInput = searchInput.replace(" ", "+");

            URL url = createURL(GOOGLE_REQUEST_URL + searchInput);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException", e);
            }

            ArrayList<Book> books = extractInfoFromJson(jsonResponse);

            return books;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            if (books == null) {
                Toast.makeText(MainActivity.this, getResources()
                        .getString(R.string.no_result), Toast.LENGTH_SHORT).show();
            }
            mAdapter.addAll(books);
            Log.i(LOG_TAG, "onPostExecute is initiated");
            mBookListView.setAdapter(mAdapter);
        }
    }

    /**
     * Returns new URL object from the given string URL.
     */

    public static URL createURL(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem with the URL", e);
        }
        return url;
    }

    // Using HttpsUrlConnection to Fetch Data
    public static String makeHttpRequest(URL url) throws IOException{

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
    public static ArrayList<Book> extractInfoFromJson(String bookJSON) {
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        ArrayList<Book> books = new ArrayList<>();

        try {
            JSONObject jsonObj = new JSONObject(bookJSON);
            JSONArray bookArray = jsonObj.getJSONArray("items");

            for (int i=0; i<bookArray.length(); i++) {
                JSONObject currentBook = bookArray.getJSONObject(i);
                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");

                String title = volumeInfo. getString("title");

                ArrayList<String> authorList = new ArrayList<String>();
                JSONArray authorJsonArray = volumeInfo.optJSONArray("authors");
                if (authorJsonArray != null) {
                    for (int j=0; j<authorJsonArray.length(); j++) {
                        authorList.add(authorJsonArray.get(j).toString());
                    }
                }
                String[] authors = authorList.toArray(new String[authorList.size()]);

                String description = volumeInfo.optString("description");

                String infoLink = volumeInfo.getString("infoLink");

                Book book = new Book(title, authors, description, infoLink);

                books.add(book);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing earthquake JSON result", e);
        }
        return books;
    }
}
