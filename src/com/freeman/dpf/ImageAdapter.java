package com.freeman.dpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.ThumbnailComparator;

public class ImageAdapter extends BaseAdapter {
    private final double KILO_PREFIX = 1024;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
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
    }

    private void createPhotoThumnbails(ArrayList<String> photos) {
        for (String photo: photos) {
            File photoFile = new File(photo);
            File thumbnailPath = getOutputDirectoryToStoreThumbnail(photoFile);
            long lastModified = photoFile.lastModified();
            String photoSize = extractSize(photoFile);
            try {
                if (thumbnailPath.exists()) {
                    ComparableThumbnail thumbnail = new ComparableThumbnail(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(thumbnailPath.getAbsolutePath()), 100, 100), lastModified, photoSize, false); 
                    thumbnails.add(thumbnail);
                    continue;
                }
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo), 100, 100);
                FileOutputStream out = new FileOutputStream(thumbnailPath);
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
                thumbnails.add(new ComparableThumbnail(thumbnail, lastModified, photoSize, false));
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
        return String.format("%.1g MB%n", bytes/KILO_PREFIX/KILO_PREFIX);
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
            holder.photoSize = (TextView) convertView.findViewById(R.id.size);
            holder.lastModified = (TextView) convertView.findViewById(R.id.lastModified);
            holder.selectedThumbnail = (CheckBox) convertView.findViewById(R.id.selected);
//            holder.checkbox = (CheckBox) convertView.findViewById(R.id.photoCheckBox);
            convertView.setTag(holder);
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
        holder.photoSize.setText(thumbnail.getSize());
        holder.lastModified.setText(formatter.format(thumbnail.getLastModified()));
        holder.selectedThumbnail.setChecked(thumbnail.isSelected());
        holder.selectedThumbnail.setId(position);

        holder.selectedThumbnail.setOnClickListener(new OnClickListener() {
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
        processThumbnailState();

        return convertView;
    }

    private void processThumbnailState() {
        for(Integer index: remainingThumbnailIndices) {
            thumbnails.get(index).setSelected(true);
        }
    }

    class ViewHolder {
        ImageView thumbanilView;
        TextView photoSize;
        TextView lastModified;
        CheckBox selectedThumbnail;
    }
}
