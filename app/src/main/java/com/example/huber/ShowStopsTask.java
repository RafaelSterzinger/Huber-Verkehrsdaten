package com.example.huber;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ShowStopsTask extends AsyncTask<LatLng, Integer, List<Station>> {
    private final HuberDataBase dataBase;
    private final GoogleMap map;
    private final Map<Integer, Station> currentStations;
    private Runnable callback;
    private int walkSpeed = 4;
    private Location location;

    private static final int STATIONS_AMOUNT = 40;

    private ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations) {
        this.dataBase = dataBase;
        this.map = map;
        this.currentStations = currentStations;
    }

    ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations, Runnable callback, int walkSpeed, Location location) {
        this.dataBase = dataBase;
        this.map = map;
        this.currentStations = currentStations;
        this.callback = callback;
        this.walkSpeed = walkSpeed;
        this.location = location;
    }

    @Override
    protected List<Station> doInBackground(LatLng... latLngs) {
        double centerLon = (Math.abs(latLngs[0].longitude - latLngs[1].longitude) / 2.0) + latLngs[1].longitude;
        double centerLat = (Math.abs(latLngs[0].latitude - latLngs[1].latitude) / 2.0) + latLngs[1].latitude;

        // Filter results depending on distance to center
        List<Station> stations = dataBase.stationDao().getInBound(latLngs[0].longitude, latLngs[0].latitude, latLngs[1].longitude, latLngs[1].latitude);
        stations = stations.stream().peek(station -> {
            double distance = location != null ? DistanceCalculatorHaversine.distance(location.getLatitude(), location.getLongitude(), station.getLat(), station.getLon()) : 0;
            station.setDistanceKm(distance);
            station.setDistanceHours((int)(distance/walkSpeed));
            station.setDistanceMinutes((int)(distance/walkSpeed * 60) % 60);
        }).collect(Collectors.toList());

        stations.sort((st1, st2) -> {
            double distance1 = getDistance(centerLon, centerLat, st1);                                        // sort after approximate distance to center
            double distance2 = getDistance(centerLon, centerLat, st2);
            return Double.compare(distance1, distance2);
            //return Double.compare(st1.getDistanceKm(), st2.getDistanceKm());
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
            // TODO favourites must contain marker
            Marker marker = station.getMarker();
            if (marker != null) {
                marker.remove();
            }
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
        super.onPostExecute(stations);
    }
}
