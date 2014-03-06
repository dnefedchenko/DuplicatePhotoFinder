package com.freeman.dpf.sort.date;

import java.util.Date;

import android.graphics.Bitmap;

public class ComparableThumbnail {
    private Bitmap thumbnail;
    private Date lastModified;

    public ComparableThumbnail(Bitmap thumbnail, long lastModified) {
        this.thumbnail = thumbnail;
        this.lastModified = new Date(lastModified);
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
