package com.freeman.dpf.sort.date;

import java.util.Comparator;

public class ThumbnailComparator implements Comparator<ComparableThumbnail> {
    @Override
    public int compare(ComparableThumbnail first, ComparableThumbnail second) {
        return first.getLastModified().compareTo(second.getLastModified());
    }
}
