package com.example.huber.task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GetStationFavoritesTask extends AsyncTask<LatLng, Integer, List<Station>> {

    private final HuberDataBase dataBase;
    private final Runnable callback;
    private Map<Integer, Station> favoriteStations;


    public GetStationFavoritesTask(HuberDataBase dataBase, Runnable callback, Map<Integer, Station> favoriteStations) {
        this.dataBase = dataBase;
        this.callback = callback;
        this.favoriteStations = favoriteStations;
    }

    @Override
    protected List<Station> doInBackground(LatLng... latLngs) {
        List<Station> stationList = this.dataBase.stationDao().getFavoriteStations();
        Log.d("GetStationFavoritesTask", "doInBackground: " + stationList);
        stationList.sort((station1, station2) -> station1.getName().compareTo(station2.getName()));
        return stationList;
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        favoriteStations.clear();
        stations.forEach( station -> {
                    favoriteStations.put(station.getUid(), station);
            Log.d("GetStationFavoriteTask", "onPostExecute: " + station);
                }
        );
        callback.run();
        super.onPostExecute(stations);
    }
}
