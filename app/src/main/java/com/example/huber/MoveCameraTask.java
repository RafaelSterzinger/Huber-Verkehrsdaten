package com.example.huber;

import android.os.AsyncTask;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class MoveCameraTask extends AsyncTask<Integer, Integer, Station> {
    private HuberDataBase dataBase;
    private GoogleMap map;

    MoveCameraTask(HuberDataBase dataBase, GoogleMap map) {
        this.dataBase = dataBase;
        this.map = map;
    }

    @Override
    protected Station doInBackground(Integer... integers) {
        return dataBase.stationDao().getStationWithUID(integers[0]);
    }

    @Override
    protected void onPostExecute(Station station) {
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(station.getLat(), station.getLon())));
        super.onPostExecute(station);
    }
}
