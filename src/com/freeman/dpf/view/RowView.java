package com.freeman.dpf.view;

import android.view.View;

public interface RowView {
    public View getView(View convertView);
    public int getViewType();
}
