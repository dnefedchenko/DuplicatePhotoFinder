package com.freeman.dpf.model.util;

public enum RowViewType {
    FULL_VEW(0),
    SEPARATOR_VIEW(1);

    private int type;

    RowViewType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
