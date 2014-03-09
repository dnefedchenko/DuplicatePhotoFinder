package com.freeman.dpf;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

/**
 *  This activity lists suggestions of photos which should remain.
 * 
 * @author freeman
 *
 */
public class PhotoListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection_view);

        Intent intentToListAllDcimPhotos = getIntent();
        ArrayList<String> photoPaths = intentToListAllDcimPhotos.getStringArrayListExtra(MainActivity.PHOTO_EXTRAS);

//        GridView suggestionGrid = (GridView) findViewById(R.id.suggestionGrid);
        GridView suggestionGrid = (GridView) findViewById(R.id.thumbnailsGrid);
        suggestionGrid.setAdapter(new ImageAdapter(this, photoPaths));

        suggestionGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(PhotoListActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
