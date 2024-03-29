package com.example.huber.task;

import android.os.AsyncTask;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.function.Consumer;

public class MoveCameraTask extends AsyncTask<Integer, Integer, Station> {
    private final HuberDataBase dataBase;
    private final GoogleMap map;
    private final Consumer<Station> consumer;

    public MoveCameraTask(HuberDataBase dataBase, GoogleMap map, Consumer<Station> consumer) {
        this.dataBase = dataBase;
        this.map = map;
        this.consumer = consumer;
    }


    @Override
    protected Station doInBackground(Integer... integers) {
        return dataBase.stationDao().getStationWithUID(integers[0]);
    }

    @Override
    protected void onPostExecute(Station station) {
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(station.getLat(), station.getLon())));
        consumer.accept(station);
        super.onPostExecute(station);
    }
}
