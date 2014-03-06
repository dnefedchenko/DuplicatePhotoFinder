package com.freeman.dpf.sort.date;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreationDateSorter {

    public Map<Date, List<File>> sort(List<File> photosToBeSorted) {
        Map<Date, List<File>> sorted = new HashMap<Date, List<File>>();
        sorted.put(new Date(), photosToBeSorted);
        return sorted;
    }

}
