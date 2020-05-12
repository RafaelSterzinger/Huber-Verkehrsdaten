package com.example.huber;

import android.os.AsyncTask;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

class ShowStopsTask extends AsyncTask<LatLng, Integer, List<Station>> {
    private HuberDataBase dataBase;
    private GoogleMap map;
    private Map<Integer, Station> currentStations;
    private Runnable callback;

    private static final int STATIONS_AMOUNT = 8;

    private ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations) {
        this.dataBase = dataBase;
        this.map = map;
        this.currentStations = currentStations;
    }

    ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations, Runnable callback) {
        this.dataBase = dataBase;
        this.map = map;
        this.currentStations = currentStations;
        this.callback = callback;
    }

    @Override
    protected List<Station> doInBackground(LatLng... latLngs) {
        double centerLon = (Math.abs(latLngs[0].longitude - latLngs[1].longitude) / 2.0) + latLngs[1].longitude;
        double centerLat = (Math.abs(latLngs[0].latitude - latLngs[1].latitude) / 2.0) + latLngs[1].latitude;

        // Filter results depending on distance to center
        List<Station> stations = dataBase.stationDao().getInBound(latLngs[0].longitude, latLngs[0].latitude, latLngs[1].longitude, latLngs[1].latitude);
        stations.sort((st1, st2) -> {
            double distance1 = getDistance(centerLon, centerLat, st1);
            double distance2 = getDistance(centerLon, centerLat, st2);
            return Double.compare(distance1, distance2);
        });

        // Limit results
        int results = stations.size();
        return results < STATIONS_AMOUNT ? stations.subList(0, results) : stations.subList(0, STATIONS_AMOUNT);
    }

    private double getDistance(double centerLon, double centerLat, Station station) {
        double distanceLon = Math.abs(station.getLon() - centerLon);
        double distanceLat = Math.abs(station.getLat() - centerLat);
        return Math.sqrt(distanceLat * distanceLat + distanceLon * distanceLon);
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        currentStations.values().stream().filter(station -> !stations.contains(station)).forEach(station -> {
            Objects.requireNonNull(station.getMarker()).remove();
            currentStations.remove(station.getUid());
        });
        stations.stream().filter(station -> !currentStations.containsKey(station.getUid())).forEach(station -> {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(station.getLat(), station.getLon()));
            markerOptions.title(station.getName());
            station.setMarker(map.addMarker(markerOptions));
            currentStations.put(station.getUid(), station);
        });
        if (callback != null) {
            callback.run();
        }
    }
}