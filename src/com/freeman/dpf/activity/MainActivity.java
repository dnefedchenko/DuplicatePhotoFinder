package com.freeman.dpf.activity;

import static android.os.Environment.getExternalStoragePublicDirectory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.freeman.dpf.R;

/**
 *  Duplicate photo finder main activity.
 * 
 * @author freeman
 *
 */
public class MainActivity extends Activity {
    public final static String PHOTO_EXTRAS = "com.freeman.photofinder.PHOTO_EXTRAS";
    private final static int FADE_IN_TRANSITION_MS = 1000;

    private SeekBar comparisonSeverityBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        comparisonSeverityBar = (SeekBar) findViewById(R.id.comparison_seekbar);
        comparisonSeverityBar.setProgress(9);
        comparisonSeverityBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ImageView comparisonSeverityExample = (ImageView) findViewById(R.id.second_comparison_sample);
                TransitionDrawable comparisonSeverityDrawable = null;

                if (progress >= 0 && progress <= 3) {
                    comparisonSeverityDrawable = new TransitionDrawable(new Drawable[] {comparisonSeverityExample.getDrawable(), getResources().getDrawable(R.drawable.one_event)});
                } else if (progress > 3 && progress <= 7) {
                    comparisonSeverityDrawable = new TransitionDrawable(new Drawable[] {comparisonSeverityExample.getDrawable(), getResources().getDrawable(R.drawable.quite_similar)});
                } else if (progress > 7 && progress <= 10) {
                    comparisonSeverityDrawable = new TransitionDrawable(new Drawable[] {comparisonSeverityExample.getDrawable(), getResources().getDrawable(R.drawable.identical)});
                }
                comparisonSeverityExample.setImageDrawable(comparisonSeverityDrawable);
                comparisonSeverityDrawable.startTransition(FADE_IN_TRANSITION_MS);
            }
        });
    }

    private boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void findSimilarPhotos(View view) throws IOException {
        Intent intentToListAllDcimPhotos = new Intent(this, SuggestionListActivity.class);
        intentToListAllDcimPhotos.putStringArrayListExtra(PHOTO_EXTRAS, getDcimFiles());
        startActivity(intentToListAllDcimPhotos);
    }

    private ArrayList<String> getDcimFiles() throws IOException {
        File dcim = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
        if (!isExternalStorageMounted()) {
            System.out.println("Media storage is not mounted.");
            return null;
        }

        FilenameFilter photoExtensionFilter = new FilenameFilter() {
            String fileExtension = ".jpg";

            @Override
            @SuppressLint("DefaultLocale")
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(fileExtension) ? true : false;
            }
        };

        File[] photoFiles = dcim.listFiles(photoExtensionFilter);
        ArrayList<String> photos = new ArrayList<String>(photoFiles.length);
        for (int i = 0; i < photoFiles.length - 1; i++) {
            photos.add(photoFiles[i].getAbsolutePath());
        }
        return photos;
    }
}

