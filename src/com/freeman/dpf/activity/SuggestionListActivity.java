package com.freeman.dpf.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.freeman.dpf.R;
import com.freeman.dpf.adapter.SuggestionAdapter;
import com.freeman.dpf.model.ComparableThumbnail;
import com.freeman.dpf.model.ThumbnailComparator;
import com.freeman.dpf.view.RowView;

public class SuggestionListActivity extends ListActivity {
    private static final int DIALOG_KEY = 0;

    private BigDecimal KILO_PREFIX = new BigDecimal("1024");
    private final List<RowView> rows;

    private List<Integer> separatorViewIndices;
    private ProgressDialog progressDialog;

    public SuggestionListActivity() {
        this.rows = new ArrayList<RowView>();
        this.separatorViewIndices = new ArrayList<Integer>();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DIALOG_KEY:
                progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("Galery scanning...");
                progressDialog.setCancelable(false);
                return progressDialog;
        }
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestion_list_activity_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intentToListAllDcimPhotos = getIntent();

        final ArrayList<String> photoPaths = intentToListAllDcimPhotos.getStringArrayListExtra(MainActivity.PHOTO_EXTRAS);

        showDialog(DIALOG_KEY);
        new ThumbnailProcessor().execute(photoPaths.toArray(new String[] {}));
    }

    private File getOutputDirectoryToStoreThumbnail(File photo) {
        if (!isExternalStorageMounted()) {
            return null;
        }

        File directoryToStoreThumbnail = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
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

    private String extractSize(File photoFile) {
        BigDecimal bytes = new BigDecimal(photoFile.length());
        BigDecimal kilobytes = bytes.divide(KILO_PREFIX, 2, RoundingMode.UP);
        BigDecimal megabytes = kilobytes.divide(KILO_PREFIX, 1, RoundingMode.UP);
        return megabytes.toString() + "MB";
    }

    private void splitOnGroupsByDate(List<ComparableThumbnail> thumbnails) {
        ListIterator<ComparableThumbnail> iterator = thumbnails.listIterator();
        int previousYear = -1;
        int previousMonth = 0;
        int previousDay = 0;
        int previousHour = 0;
        int previousMinute = 0;
        int previousSecond = 0;

        BigDecimal totalGroupSize = new BigDecimal("0");

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
                totalGroupSize = totalGroupSize.add(new BigDecimal(thumbnail.getSize().substring(0, thumbnail.getSize().lastIndexOf('M'))));
            } else {
                iterator.previous();
                iterator.add(new ComparableThumbnail(null, 0, totalGroupSize.toString()+" MB"));
                totalGroupSize = new BigDecimal("0");
                separatorViewIndices.add(iterator.nextIndex() - 1);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.suggestion_list_activity_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.duplicates_amount_text);
        TextView duplicateTextView = (TextView) item.getActionView().findViewById(R.id.duplicate_text);
        duplicateTextView.setText("582 duplicates(1.32 GB)");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteSelectedDuplicates();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSelectedDuplicates() {
        Toast.makeText(this, "Performing duplicate deleting...", Toast.LENGTH_SHORT).show();
    }

    class ThumbnailProcessor extends AsyncTask<String, Integer, List<ComparableThumbnail>> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected List<ComparableThumbnail> doInBackground(String... photos) {
            List<ComparableThumbnail> thumbnails = new ArrayList<ComparableThumbnail>();

            for (int i = 0; i < photos.length; i++) {
                File photoFile = new File(photos[i]);
                File thumbnailPath = getOutputDirectoryToStoreThumbnail(photoFile);
                long lastModified = photoFile.lastModified();
                String photoSize = extractSize(photoFile);
                try {
                    if (thumbnailPath.exists()) {
                        ComparableThumbnail thumbnail = new ComparableThumbnail(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(thumbnailPath.getAbsolutePath()), 100, 100), lastModified, photoSize); 
                        thumbnails.add(thumbnail);
                        publishProgress((int)(((i + 1)/(float)photos.length)*100));
                        continue;
                    }
                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photos[i]), 100, 100);
                    FileOutputStream out = new FileOutputStream(thumbnailPath);
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    thumbnails.add(new ComparableThumbnail(thumbnail, lastModified, photoSize));
                    out.close();
                    publishProgress((int)(((i + 1)/(float)photos.length)*100));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(thumbnails, new ThumbnailComparator());

            splitOnGroupsByDate(thumbnails);
            return thumbnails;
        }

        @Override
        protected void onProgressUpdate(Integer... progressValues) {
            progressDialog.setProgress(progressValues[0]);
        }

        @Override
        protected void onPostExecute(List<ComparableThumbnail> thumbnails) {
            setListAdapter(new SuggestionAdapter(SuggestionListActivity.this, rows, thumbnails));
            progressDialog.dismiss();
        }
    }
}
