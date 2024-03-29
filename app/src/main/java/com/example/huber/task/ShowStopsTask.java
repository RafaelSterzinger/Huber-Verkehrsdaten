package com.example.huber.task;

import android.location.Location;
import android.os.AsyncTask;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowStopsTask extends AsyncTask<LatLng, Integer, List<Station>> {
    private static final int STATIONS_AMOUNT = 5;
    private final HuberDataBase dataBase;
    private final GoogleMap map;
    private final Map<Integer, Station> currentStations;
    private final Runnable callback;
    private final int walkSpeed;
    private final Location location;
    private final boolean onlyFavorites;

    public ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations, Runnable callback, int walkSpeed, Location location, boolean onlyFavorites) {
        this.dataBase = dataBase;
        this.map = map;
        this.currentStations = currentStations;
        this.callback = callback;
        this.walkSpeed = walkSpeed;
        this.location = location;
        this.onlyFavorites = onlyFavorites;
    }

    @Override
    protected List<Station> doInBackground(LatLng... latLngs) {
        double centerLon = (Math.abs(latLngs[0].longitude - latLngs[1].longitude) / 2.0) + latLngs[1].longitude;            // center of the screen if not favorites - current location if favorites
        double centerLat = (Math.abs(latLngs[0].latitude - latLngs[1].latitude) / 2.0) + latLngs[1].latitude;
        LatLng locationLatLng = location == null ? null : new LatLng(location.getLatitude(), location.getLongitude());

        // Filter results depending on distance to center
        List<Station> stations = onlyFavorites ? dataBase.stationDao().getFavoriteStations()
                : dataBase.stationDao().getInBound(latLngs[0].longitude, latLngs[0].latitude, latLngs[1].longitude, latLngs[1].latitude);
        stations = stations.stream().peek(station -> station.setDistance(locationLatLng, walkSpeed)).collect(Collectors.toList());

        stations.sort((st1, st2) -> {
            double distance1 = getDistance(centerLon, centerLat, st1);                                        // sort after approximate distance to center
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
            station.removeMarkerIfExists();
            currentStations.remove(station.getUid());
        });
        // arrows are only added in onSuggestionClick()
        stations.stream().filter(station -> !currentStations.containsKey(station.getUid())).forEach(station -> {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(station.getLat(), station.getLon()));
            markerOptions.title(station.getName());
            if (station.getFavorite()) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            }
            station.setMarker(map.addMarker(markerOptions));
            currentStations.put(station.getUid(), station);
        });
        if (callback != null) {
            callback.run();
        }
        super.onPostExecute(stations);
    }
}
