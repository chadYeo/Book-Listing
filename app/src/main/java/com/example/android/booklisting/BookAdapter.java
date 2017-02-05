package com.example.android.booklisting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Context context, List<Book> objects) {
        super(context, 0, objects);
    }

    private List<Book> books = new ArrayList<>();

    private static class ViewHolder {
        TextView mTitleTextView;
        TextView mAuthorTextView;
        TextView mSummaryTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.book_info, parent, false);
        }

        Book currentPosition = getItem(position);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.mTitleTextView = (TextView) convertView.findViewById(R.id.title_textView);
        String title = currentPosition.getTitle();
        viewHolder.mTitleTextView.setText(title);

        viewHolder.mAuthorTextView = (TextView) convertView.findViewById(R.id.author_textView);
        String author = currentPosition.getAuthor();
        viewHolder.mAuthorTextView.setText(author);

        viewHolder.mSummaryTextView = (TextView) convertView.findViewById(R.id.summary_textView);
        String summary = currentPosition.getSummary();
        viewHolder.mSummaryTextView.setText(summary);

        return convertView;
    }
}
