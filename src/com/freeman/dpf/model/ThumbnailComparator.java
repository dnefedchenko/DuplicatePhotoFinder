package com.freeman.dpf.model;

import java.util.Comparator;

public class ThumbnailComparator implements Comparator<ComparableThumbnail> {
    @Override
    public int compare(ComparableThumbnail first, ComparableThumbnail second) {
        return first.getLastModified().compareTo(second.getLastModified());
    }
}
