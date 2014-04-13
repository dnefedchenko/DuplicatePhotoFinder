package com.freeman.dpf.view;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.freeman.dpf.R;
import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.util.RowViewType;

public class FullView implements RowView {
    private LayoutInflater inflater;
    private ComparableThumbnail thumbnail;

    public FullView(LayoutInflater inflater, ComparableThumbnail thumbnail) {
        this.inflater = inflater;
        this.thumbnail = thumbnail;
    }

    
    @Override
    public View getView(View convertView) {
        ViewHolder holder;
        View view;
        if (convertView == null) {
            ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.full_layout, null);
            holder = new ViewHolder((ImageView) viewGroup.findViewById(R.id.thumbnail),
                    (TextView) viewGroup.findViewById(R.id.lastModified),
                    (TextView) viewGroup.findViewById(R.id.size),
                    (CheckBox) viewGroup.findViewById(R.id.selected));
            viewGroup.setTag(holder);
            view = viewGroup;
        } else {
            holder = (ViewHolder) convertView.getTag();
            view = convertView;
        }

        holder.thumbnail.setImageBitmap(thumbnail.getThumbnail());
        holder.size.setText(thumbnail.getSize());
        holder.lastModified.setText(new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US).format(thumbnail.getLastModified()));
        holder.selected.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thumbnail.isSelected()) {
                    thumbnail.setSelected(false);
                } else {
                    thumbnail.setSelected(true);
                }
            }
        });
        holder.selected.setChecked(thumbnail.isSelected());
        return view;
    }

    @Override
    public int getViewType() {
        return RowViewType.FULL_VEW.getType();
    }

    private static class ViewHolder {
        final ImageView thumbnail;
        final TextView lastModified;
        final TextView size;
        final CheckBox selected;

        private ViewHolder(ImageView thumbnail, TextView lastModified, TextView size, CheckBox selected) {
            this.thumbnail = thumbnail;
            this.lastModified = lastModified;
            this.size = size;
            this.selected = selected;
        }
    }
}
