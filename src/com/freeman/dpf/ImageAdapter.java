package com.freeman.dpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.freeman.dpf.sort.date.ComparableThumbnail;
import com.freeman.dpf.sort.date.ThumbnailComparator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<ComparableThumbnail> thumbnails;

    public ImageAdapter(Context context, ArrayList<String> photos) {
        this.context = context;
        this.thumbnails = new ArrayList<ComparableThumbnail>();
        createPhotoThumnbails(photos);
        Collections.sort(thumbnails, new ThumbnailComparator());
    }

    private void createPhotoThumnbails(ArrayList<String> photos) {
        for (String photo: photos) {
            File thumbnailPath = getOutputDirectoryToStoreThumbnail(photo);
            long lastModified = Long.parseLong(thumbnailPath.getName().substring(0, thumbnailPath.getName().lastIndexOf('.')));
            try {
                if (thumbnailPath.exists()) {
                    ComparableThumbnail thumbnail = new ComparableThumbnail(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(thumbnailPath.getAbsolutePath()), 100, 100), lastModified); 
                    thumbnails.add(thumbnail);
                    continue;
                }
                Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo), 100, 100);
                FileOutputStream out = new FileOutputStream(thumbnailPath);
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
                thumbnails.add(new ComparableThumbnail(thumbnail, lastModified));
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getOutputDirectoryToStoreThumbnail(String photo) {
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

        String thumbnailName = String.valueOf(new File(photo).lastModified())+".jpg";;
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
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(175, 175));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(thumbnails.get(position).getThumbnail());

        return imageView;
    }
}
