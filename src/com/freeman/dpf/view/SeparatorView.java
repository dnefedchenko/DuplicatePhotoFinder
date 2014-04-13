package com.freeman.dpf.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.freeman.dpf.R;
import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.util.RowViewType;

public class SeparatorView implements RowView {
    private LayoutInflater inflater;
    private ComparableThumbnail thumbnail;

    public SeparatorView(LayoutInflater inflater, ComparableThumbnail thumbnail) {
        this.inflater = inflater;
        this.thumbnail = thumbnail;
    }

    @Override
    public View getView(View convertView) {
        ViewHolder holder;
        View view;
        if (convertView == null) {
            ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.separator_layout, null);
            holder = new ViewHolder((TextView) viewGroup.findViewById(R.id.size));
            viewGroup.setTag(holder);
            view = viewGroup;
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        holder.size.setText(thumbnail.getSize());
        return view;
    }

    @Override
    public int getViewType() {
        return RowViewType.SEPARATOR_VIEW.getType();
    }

    private static class ViewHolder {
        final TextView size;

        private ViewHolder(TextView size) {
            this.size = size;
        }
    }
}
