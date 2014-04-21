package com.freeman.dpf.activity;

import static android.os.Environment.getExternalStoragePublicDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.freeman.dpf.R;

/**
 *  Duplicate photo finder main activity.
 * 
 * @author freeman
 *
 */
public class MainActivity extends Activity {
    public final static String PHOTO_EXTRAS = "com.freeman.photofinder.PHOTO_EXTRAS";

    private SeekBar comparisonSeverityBar;
    private TextView comparisonSeverityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        comparisonSeverityBar = (SeekBar) findViewById(R.id.comparison_seekbar);
        comparisonSeverityBar.setOnSeekBarChangeListener(null);
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
        File[] photoFiles = dcim.listFiles();
        ArrayList<String> photos = new ArrayList<String>(photoFiles.length);
        for (int i = 0; i < photoFiles.length - 1; i++) {
            photos.add(photoFiles[i].getAbsolutePath());
        }
        return photos;
    }
}

