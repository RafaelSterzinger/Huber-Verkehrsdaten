package com.example.huber.task;

import android.os.AsyncTask;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;

public class UpdateDBStationFavoriteTask extends AsyncTask<Station, Integer, Boolean> {
    private final HuberDataBase dataBase;
    private Station station;

    public UpdateDBStationFavoriteTask(HuberDataBase dataBase) {
        this.dataBase = dataBase;
    }


    @Override
    protected Boolean doInBackground(Station... stations) {
        station = stations[0];
        dataBase.stationDao().updateFavourite(station.getUid(), !station.getFavourite());
        return ! station.getFavourite();
    }

    @Override
    protected void onPostExecute(Boolean newFavourite) {
        station.setFavourite(newFavourite);     // must be done in onPostExecute/onUIThread since we change the Marker color
        super.onPostExecute(newFavourite);
    }
}



