package com.freeman.dpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.ThumbnailComparator;

public class SuggestionListAdapter extends BaseAdapter {
    private final double KILO_PREFIX = 1024.0;

    private Context context;
    private List<ComparableThumbnail> thumbnails;

    public SuggestionListAdapter(Context context, ArrayList<String> photos) {
        this.context = context;
        this.thumbnails = new ArrayList<ComparableThumbnail>();
        createPhotoThumnbails(photos);
        Collections.sort(thumbnails, new ThumbnailComparator());
        splitOnGroupsByDateSimilarity();
    }

    private void createPhotoThumnbails(ArrayList<String> photos) {
        for (String photo: photos) {
            File photoFile = new File(photo);
            File thumbnailPath = getOutputDirectoryToStoreThumbnail(photoFile);
            long lastModified = photoFile.lastModified();
            String photoSize = extractSize(photoFile);
            try {
                if (thumbnailPath.exists()) {
                    ComparableThumbnail thumbnail = new ComparableThumbnail(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(thumbnailPath.getAbsolutePath()), 100, 100), lastModified, photoSize); 
                    thumbnails.add(thumbnail);
                    continue;
                }
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo), 100, 100);
                FileOutputStream out = new FileOutputStream(thumbnailPath);
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
                thumbnails.add(new ComparableThumbnail(thumbnail, lastModified, photoSize));
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getOutputDirectoryToStoreThumbnail(File photo) {
        if (!isExternalStorageMounted()) {
            return null;
        }

        File directoryToStoreThumbnail = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getApplicationContext().getPackageName()
                + "/Files");

        if (! directoryToStoreThumbnail.exists()){
            if (! directoryToStoreThumbnail.mkdirs()){
                return null;
            }
        }

        String thumbnailName = String.valueOf(photo.lastModified())+".jpg";
        return new File(directoryToStoreThumbnail.getPath() + File.separator + thumbnailName);
    }

    private boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    @SuppressLint("DefaultLocale")
    private String extractSize(File photoFile) {
        double bytes = photoFile.length();
        DecimalFormat format = new DecimalFormat("@@");
        return format.format(bytes/KILO_PREFIX/KILO_PREFIX).replace(',', '.')+" MB";
    }

    private void splitOnGroupsByDateSimilarity() {
        ListIterator<ComparableThumbnail> iterator = thumbnails.listIterator();
        int previousYear = -1;
        int previousMonth = 0;
        int previousDay = 0;
        int previousHour = 0;
        int previousMinute = 0;
        int previousSecond = 0;
        double totalGroupSize = 0;

        while (iterator.hasNext()) {
            ComparableThumbnail thumbnail = iterator.next();
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTime(thumbnail.getLastModified());

            int year = lastModified.get(Calendar.YEAR);
            int month = lastModified.get(Calendar.MONTH);
            int day = lastModified.get(Calendar.DATE);
            int hour = lastModified.get(Calendar.HOUR_OF_DAY);
            int minute = lastModified.get(Calendar.MINUTE);
            int second = lastModified.get(Calendar.SECOND);

            if (previousYear == -1 || previousYear == year && previousMonth == month && previousDay == day
                    && previousHour == hour || (hour - previousHour == 1 && Math.abs(previousMinute - 60) <= 2)
                    && (previousMinute == minute || Math.abs(previousMinute - minute) <= 2) || (hour - previousHour == 1 && Math.abs(previousMinute - (60 + minute)) <= 2)
                    && (previousSecond == second || Math.abs(previousSecond - second) < 60)){
                totalGroupSize += Double.parseDouble(thumbnail.getSize().substring(0, thumbnail.getSize().lastIndexOf('M')-1));
            } else {
                iterator.previous();
                iterator.add(new ComparableThumbnail(null, 0, String.valueOf(totalGroupSize)+" MB"));
                totalGroupSize = 0;
            }

            previousYear = year;
            previousMonth = month;
            previousDay = day;
            previousHour = hour;
            previousMinute = minute;
            previousSecond = second;
        }
    }

    @Override
    public int getCount() {
        return thumbnails.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView thumbnailView;
        TextView photoSize;
        TextView lastModified;
        TextView separatorView;
        CheckBox selectedThumbnail;

        LinearLayout layout = new LinearLayout(context);

        if (convertView == null) {
            ComparableThumbnail thumbnail = thumbnails.get(position);
            if (thumbnail.getThumbnail() != null) {
//                createFullView(layout, thumbnail);

              thumbnailView = new ImageView(context);
              thumbnailView.setImageBitmap(thumbnails.get(position).getThumbnail());
              thumbnailView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
  
              layout = new LinearLayout(context);
              layout.setOrientation(0);
              layout.setPadding(1, 1, 1, 1);
  
              layout.addView(thumbnailView);
            } else {
//                createGroupSeparator(layout, thumbnail);

                separatorView = new TextView(context);
                separatorView.setText(thumbnail.getSize());

                layout.setOrientation(0);
                layout.setPadding(1, 1, 1, 1);

                layout.addView(separatorView);
            }
        }
        else {
            layout = (LinearLayout) convertView;

            View view = layout.getChildAt(0);
            if (view.getClass().getSimpleName().equals("TextView")) {
                separatorView = (TextView) view;
                separatorView.setText(thumbnails.get(position).getSize());
            } else {
                thumbnailView = (ImageView) view;
                thumbnailView.setImageBitmap(thumbnails.get(position).getThumbnail());
            }
        }
        return layout;
    }

    private void createFullView(LinearLayout layout, ComparableThumbnail thumbnail) {
        ImageView thumbnailView = new ImageView(context);
        thumbnailView.setImageBitmap(thumbnail.getThumbnail());
        thumbnailView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));

        layout = new LinearLayout(context);
        layout.setOrientation(0);
        layout.setPadding(1, 1, 1, 1);

        layout.addView(thumbnailView);
    }

    private void createGroupSeparator(LinearLayout layout, ComparableThumbnail thumbnail) {
        TextView separatorView = new TextView(context);
        separatorView.setText(thumbnail.getSize());

        layout.setOrientation(0);
        layout.setPadding(1, 1, 1, 1);

        layout.addView(separatorView);
    }
}
