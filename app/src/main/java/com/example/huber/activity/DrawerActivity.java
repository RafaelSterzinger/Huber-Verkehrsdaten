package com.example.huber.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.huber.R;
import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DrawerActivity extends AppCompatActivity {
    final Map<Integer, Station> favoriteStations = new ConcurrentHashMap<>();
    HuberDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_item);

        dataBase = HuberDataBase.Companion.invoke(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            //setResult(RESULT_OK);
            //finish();
            Log.d("Draweritemactivity pressed BACK", "onOptionsItemSelected: back");
            //NavUtils.navigateUpFromSameTask(this);
            //onBackPressed();
            Log.d("FINISH NAVIGATE UP FROM", "onSuggestionClick: finished");
            closeAndFinish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        closeAndFinish();
    }

    void closeAndFinish() {
        finish();
        //NavUtils.navigateUpFromSameTask(this);
    }
}
