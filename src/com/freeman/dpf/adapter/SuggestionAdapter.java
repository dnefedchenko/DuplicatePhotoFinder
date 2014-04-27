package com.freeman.dpf.adapter;

import java.util.List;

import com.freeman.dpf.activity.SuggestionListActivity;
import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.util.RowViewType;
import com.freeman.dpf.view.FullView;
import com.freeman.dpf.view.RowView;
import com.freeman.dpf.view.SeparatorView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SuggestionAdapter extends BaseAdapter {
    private List<RowView> rows;

    public SuggestionAdapter(Context context, List<RowView> rows, List<ComparableThumbnail> thumbnails) {
        this.rows = rows;

        for (ComparableThumbnail thumbnail: thumbnails) {
            if (thumbnail.getThumbnail() != null) {
                rows.add(new FullView(LayoutInflater.from(context), thumbnail));
            } else {
                rows.add(new SeparatorView(LayoutInflater.from(context), thumbnail));
            }
        }
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getViewType();
    }

    @Override
    public int getViewTypeCount() {
        return RowViewType.values().length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return rows.get(position).getView(convertView);
    }
}
