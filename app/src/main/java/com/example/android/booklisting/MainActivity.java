package com.example.android.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mBookListView;
    private BookAdapter mAdapter;
    private EditText mEditText;
    private Button mSearchButton;
    private TextView mEmptyTextView;
    private Context mContext;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String GOOGLE_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.editText);
        mBookListView = (ListView) findViewById(R.id.listView);
        mEmptyTextView = (TextView) findViewById(R.id.empty_textView);

        // To force the keyboard to appear when app is opened
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Search button method is applied below upon click:
        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    BookAsyncTask task = new BookAsyncTask();
                    Log.i(LOG_TAG, "Search Button is clicked");
                    task.execute();

                    InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    Toast.makeText(getApplicationContext(), "There's no internet connection", Toast.LENGTH_SHORT).show();
                }
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

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();}

    /**
     * AsyncTask
     */
    public class BookAsyncTask extends AsyncTask<URL, Void, List<Book>> {

        private String searchInput = mEditText.getText().toString();

        @Override
        protected List<Book> doInBackground(URL... urls) {

            if (searchInput.length() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.no_result), Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            } else {
                searchInput = searchInput.replace(" ", "+");

                URL url = createURL(GOOGLE_REQUEST_URL + searchInput);

                String jsonResponse = "";
                try {
                    jsonResponse = makeHttpRequest(url);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException", e);
                }

                List<Book> books = extractInfoFromJson(jsonResponse);

                return books;
            }
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            if (books != null && !books.isEmpty()) {
                mAdapter.clear();
                mAdapter.addAll(books);
                mBookListView.setAdapter(mAdapter);
                mEmptyTextView.setVisibility(View.GONE);
            }
            Log.i(LOG_TAG, "onPostExecute is initiated");
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createURL(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Problem with the URL", e);
            }
            return url;
        }

        // Using HttpsUrlConnection to Fetch Data
        private String makeHttpRequest(URL url) throws IOException{

            String jsonResult = "";

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
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
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResult;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
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
        private ArrayList<Book> extractInfoFromJson(String bookJSON) {
            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }

            ArrayList<Book> books = new ArrayList<>();

            try {
                JSONObject jsonObj = new JSONObject(bookJSON);
                if (jsonObj.getInt("totalItems") == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.no_result), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return null;
                }
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
}
