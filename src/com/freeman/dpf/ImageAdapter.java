package com.freeman.dpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.ThumbnailComparator;

public class ImageAdapter extends BaseAdapter {
    private final double KILO_PREFIX = 1024.0;
    private Context context;
    private List<ComparableThumbnail> thumbnails;
    private LayoutInflater inflater;
    private List<Integer> remainingThumbnailIndices;

    public ImageAdapter(Context context, ArrayList<String> photos) {
        this.context = context;
        this.thumbnails = new ArrayList<ComparableThumbnail>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        remainingThumbnailIndices = new ArrayList<Integer>();
        createPhotoThumnbails(photos);
        Collections.sort(thumbnails, new ThumbnailComparator());
        splitOnGroupsByDateSimilarity();
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
            int hour = lastModified.get(Calendar.HOUR);
            int minute = lastModified.get(Calendar.MINUTE);
            int second = lastModified.get(Calendar.SECOND);

            if (previousYear == -1 || previousYear == year && previousMonth == month && previousDay == day
                    && previousHour == hour && (previousMinute == minute || Math.abs(previousMinute - minute) <= 2)
                    && (previousSecond == second || Math.abs(previousSecond - second) < 60)){
                totalGroupSize += Double.parseDouble(thumbnail.getSize().substring(0, thumbnail.getSize().lastIndexOf('M')-1));
            } else {
                iterator.previous();
                iterator.add(new ComparableThumbnail(null, 0, String.valueOf(totalGroupSize)+" MB"));
                iterator.next();
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

    @SuppressLint("DefaultLocale")
    private String extractSize(File photoFile) {
        double bytes = photoFile.length();
        DecimalFormat format = new DecimalFormat("@@");
        return format.format(bytes/KILO_PREFIX/KILO_PREFIX).replace(',', '.')+" MB";
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
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.single_photo_view, null);

            holder.thumbanilView = (ImageView) convertView.findViewById(R.id.thumbnail);
//            holder.photoSize = (TextView) convertView.findViewById(R.id.size);
//            holder.lastModified = (TextView) convertView.findViewById(R.id.lastModified);
//            holder.selectedThumbnail = (CheckBox) convertView.findViewById(R.id.selected);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.photoCheckBox);
//            convertView.setTag(holder);
//            imageView = new ImageView(context);
//            imageView.setLayoutParams(new GridView.LayoutParams(175, 175));
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setPadding(8, 8, 8, 8);
        } else {
//            imageView = (ImageView) convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        ComparableThumbnail thumbnail  = thumbnails.get(position);
        holder.thumbanilView.setImageBitmap(thumbnail.getThumbnail());
//        holder.photoSize.setText(thumbnail.getSize());
//        holder.lastModified.setText(formatter.format(thumbnail.getLastModified()));
        holder.checkbox.setChecked(thumbnail.isSelected());
//        holder.selectedThumbnail.setId(position);

        holder.checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkbox = (CheckBox) v;

                if (!checkbox.isChecked()) {
                    remainingThumbnailIndices.remove(Integer.valueOf(checkbox.getId()));
                } else {
                    remainingThumbnailIndices.add(checkbox.getId());
                }
            }
        });
        convertView.setTag(holder);
        processThumbnailState();

        return convertView;
    }

    private void createFullPhotoView(ViewHolder holder, ComparableThumbnail thumbnail, View convertView) {
        holder.thumbanilView = new ImageView(context);
//        holder.thumbanilView.setLayoutParams(new LayoutParams(100, 100));
        holder.thumbanilView.setImageBitmap(thumbnail.getThumbnail());

//        holder.photoSize = new TextView(context);
//        RelativeLayout.LayoutParams photoSizeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        
//        holder.photoSize.setTextSize(18);
//        photoSizeParams.setMargins(105, 55, 0, 0);
//        holder.photoSize.setLayoutParams(photoSizeParams);
//        holder.photoSize.setText(thumbnail.getSize());
//
//        holder.lastModified = new TextView(context);
//        RelativeLayout.LayoutParams lastModifiedParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        lastModifiedParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        lastModifiedParams.setMargins(105, 0, 0, 0);
//        holder.photoSize.setTextSize(18);
//        holder.photoSize.setLayoutParams(lastModifiedParams);
//        holder.lastModified.setText(formatter.format(thumbnail.getLastModified()));
//
//        holder.selectedThumbnail = new CheckBox(context);
//        RelativeLayout.LayoutParams checkboxParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        checkboxParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        holder.selectedThumbnail.setChecked(thumbnail.isSelected());
//        holder.selectedThumbnail.setLayoutParams(checkboxParams);
//        holder.selectedThumbnail.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CheckBox checkbox = (CheckBox) v;
//
//                if (!checkbox.isChecked()) {
//                    remainingThumbnailIndices.remove(Integer.valueOf(checkbox.getId()));
//                } else {
//                    remainingThumbnailIndices.add(checkbox.getId());
//                }
//            }
//        });
    }

    private void createGroupTotalSizeSeparator(ViewHolder holder, ComparableThumbnail thumbnail, View convertView) {
        holder.photoSize = new TextView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(105, 55, 0, 0);
//        holder.photoSize.setLayoutParams(params);
        holder.photoSize.setText(thumbnail.getSize());
    }

    private void processThumbnailState() {
        for(Integer index: remainingThumbnailIndices) {
            thumbnails.get(index).setSelected(true);
        }
    }

    class ViewHolder {
        ImageView thumbanilView;
        TextView photoSize;
//        TextView lastModified;
        CheckBox checkbox;
    }
}
