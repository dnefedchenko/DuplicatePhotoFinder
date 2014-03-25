package com.freeman.dpf;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.ListView;

public class SuggestionListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_list);

        ListView suggestionList = (ListView) findViewById(R.id.suggestionList);
        Intent intentToListAllDcimPhotos = getIntent();

        ArrayList<String> photoPaths = intentToListAllDcimPhotos.getStringArrayListExtra(MainActivity.PHOTO_EXTRAS);
        suggestionList.setAdapter(new SuggestionListAdapter(this, photoPaths));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.suggestion_list, menu);
        return true;
    }

}
