package com.example.huber.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.huber.MainActivity;
import com.example.huber.R;
import com.example.huber.database.HuberDataBase;
import com.example.huber.databinding.EntryBinding;
import com.example.huber.entity.Station;
import com.example.huber.task.GetStationFavoritesTask;
import com.example.huber.task.UpdateDBStationFavoriteTask;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DrawerItemActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    HuberDataBase dataBase;
    private Map<Integer, Station> favoriteStations = new ConcurrentHashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_item);
        //setResult(RESULT_OK);
        //finish();

        dataBase = HuberDataBase.Companion.invoke(getApplicationContext());

        // TODO: create own task for this with an updateView() callback
        new GetStationFavoritesTask(dataBase, this::updateView, favoriteStations).execute();

        String classType;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                classType = "Favoriten";
            } else {
                classType = extras.getString("type");
            }
        } else {
            classType = (String) savedInstanceState.getSerializable("type");
        }
        setTitle(classType);

        /*if ("Störungen".equals(classType)) {
            for (int id :
                    new int[]{R.id.fav1, R.id.fav2, R.id.fav3}) {
                View entryView = findViewById(id);
                View directionView1 = entryView.findViewById(R.id.fav_dir1);
                View directionView2 = entryView.findViewById(R.id.fav_dir2);

                ((MaterialButton) directionView1.findViewById(R.id.favour)).setIcon(getDrawable(R.drawable.ic_warning_black_24dp));
                directionView1.findViewById(R.id.line_number).setVisibility(View.INVISIBLE);

                directionView2.findViewById(R.id.favour).setVisibility(View.INVISIBLE);
                directionView2.findViewById(R.id.line_number).setVisibility(View.INVISIBLE);
                ((TextView) directionView2.findViewById(R.id.name)).setText(R.string.accident);
            }
        }*/

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    private void updateView() {
        LinearLayout scrollView = findViewById(R.id.scrollView);
        LayoutInflater inflater = LayoutInflater.from(this);
        Log.d("DrawerItemActivity", "updateView: " + favoriteStations);
        runOnUiThread(() -> {
            /*scrollView.removeAllViews();
            if (favoriteStations.size() <= 0) {
                TextView emptyInfo = new TextView(this);
                emptyInfo.setText(R.string.no_favorites);
                scrollView.addView(emptyInfo);      // getLayoutparams below returns null, if the view has not been added to a parent
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)emptyInfo.getLayoutParams();
                Resources r = this.getResources();
                int px = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        16,
                        r.getDisplayMetrics()
                );
                params.setMargins(px, 0, 0, 0); //substitute parameters for left, top, right, bottom
                emptyInfo.setLayoutParams(params);*/
            if (favoriteStations.size() > 0) {
                scrollView.removeAllViews();
                favoriteStations.values().forEach(station -> addEntryToView(scrollView, inflater, station));
            }
        });
    }

    private void addEntryToView(LinearLayout scrollView, LayoutInflater inflater, Station station) {
        EntryBinding binding = DataBindingUtil.inflate(inflater, R.layout.entry, scrollView, false);    //TODO other entry
        binding.setStationVar(station);

        View view = binding.getRoot();
        int stationID = Objects.requireNonNull(station).getUid();
        view.setId(stationID);
        TextView heading = view.findViewById(R.id.station);
        heading.setId(stationID);
        view.findViewById(R.id.favour_true).setId(stationID);           // setting it on the encapsulating R.id.favour_both destroys the formatting for some reason
        view.findViewById(R.id.favour_false).setId(stationID);
        view.findViewById(R.id.notify).setVisibility(View.GONE);
        scrollView.addView(view);
        RelativeLayout.LayoutParams minuteLayoutParams = (RelativeLayout.LayoutParams) view.findViewById(R.id.minute).getLayoutParams();// set width = 0
        view.findViewById(R.id.minute).setVisibility(View.GONE);
        view.findViewById(R.id.favour_both).setLayoutParams(minuteLayoutParams);
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
    private void closeAndFinish() {
        finish();
        //NavUtils.navigateUpFromSameTask(this);
    }

    public void onSuggestionClick(View view) {
        Intent data = new Intent();
        data.setData(Uri.parse(String.valueOf(view.getId())));
        setResult(MainActivity.ACTIVITY_RESULT_CODE_FAVORITE_ONSUGGESTIONCLICK, data);
        /*
        finish();
        Log.d("FINISH", "onSuggestionClick: finished");
        NavUtils.navigateUpFromSameTask(this);
        Log.d("FINISH NAVIGATE UP FROM", "onSuggestionClick: finished");
         */
        closeAndFinish();

        // call onSuggestionClick in MainActivity. maybe pass view.id ?
    }

    public void onFavouriteClick(View view) {
        Station currentStation = favoriteStations.get(view.getId());
        new UpdateDBStationFavoriteTask(dataBase).execute(currentStation);
    }
}
