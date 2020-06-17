package com.example.huber.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import com.example.huber.MainActivity;
import com.example.huber.R;
import com.example.huber.databinding.DrawerFavoritesEntryBinding;
import com.example.huber.entity.Station;
import com.example.huber.task.GetStationFavoritesTask;
import com.example.huber.task.UpdateDBStationFavoriteTask;

import java.util.Objects;

public class FavoritesActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.nav_favorites));

        new GetStationFavoritesTask(dataBase, this::updateView, favoriteStations).execute();
    }

    private void updateView() {
        LinearLayout scrollView = findViewById(R.id.scrollView);
        LayoutInflater inflater = LayoutInflater.from(this);
        Log.d("DrawerItemActivity", "updateView: " + favoriteStations);
        runOnUiThread(() -> {
            if (favoriteStations.size() > 0) {
                scrollView.removeAllViews();
                favoriteStations.values().forEach(station -> addEntryToView(scrollView, inflater, station));
            }
        });
    }

    private void addEntryToView(LinearLayout scrollView, LayoutInflater inflater, Station station) {
        DrawerFavoritesEntryBinding binding = DataBindingUtil.inflate(inflater, R.layout.drawer_favorites_entry, scrollView, false);
        binding.setStationVar(station);

        View view = binding.getRoot();
        int stationID = Objects.requireNonNull(station).getUid();
        view.setId(stationID);
        view.findViewById(R.id.station).setId(stationID);
        view.findViewById(R.id.favour_true).setId(stationID);           // setting it on the encapsulating R.id.favour_both destroys the formatting for some reason
        view.findViewById(R.id.favour_false).setId(stationID);
        scrollView.addView(view);
    }

    public void onSuggestionClick(View view) {
        Intent data = new Intent();
        data.setData(Uri.parse(String.valueOf(view.getId())));
        setResult(MainActivity.ACTIVITY_RESULT_CODE_FAVORITE_ONSUGGESTIONCLICK, data);
        closeAndFinish();
    }

    public void onFavouriteClick(View view) {
        Station currentStation = favoriteStations.get(view.getId());
        new UpdateDBStationFavoriteTask(dataBase).execute(currentStation);
    }
}
