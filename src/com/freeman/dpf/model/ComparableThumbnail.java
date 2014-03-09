package com.freeman.dpf.model;

import java.util.Date;

import android.graphics.Bitmap;

public class ComparableThumbnail {
    private Bitmap thumbnail;
    private Date lastModified;
    private String size;
    private boolean selected;

    public ComparableThumbnail(Bitmap thumbnail, long lastModified, String size, boolean selected) {
        this.thumbnail = thumbnail;
        this.lastModified = new Date(lastModified);
        this.size = size;
        this.selected = selected;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getSize() {
        return size;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
