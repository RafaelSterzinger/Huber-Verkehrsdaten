package com.example.huber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.huber.database.data.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;


import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener {

    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 0;

    private GoogleMap map;
    private LocationManager location;
    private View mapView;
    private Map<Integer, Station> currentStations = new ConcurrentHashMap<>();
    private List<Circle> currentCircle = new ArrayList<>();

    private HuberDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        //mapView = mapFragment.getView();

        createLocationManager();

        dataBase = HuberDataBase.Companion.invoke(getApplicationContext());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                createLocationManager();
        }
    }

    private void createLocationManager() {
        location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
        } else {
            location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, DISTANCE_UPDATE, this);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnCameraMoveListener(this);
        // View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        // RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

// align location button with rlp
        /*


         */
    }

    @Override
    public void onLocationChanged(Location location) {
        setDistanceCircles(location);
    }

    private void setDistanceCircles(Location location) {
        if (currentCircle != null){
            currentCircle.clear();
        }

        List<PatternItem> pattern = Collections.<PatternItem>singletonList(new Dot());
        CircleOptions circleOptions = new CircleOptions().strokeColor(getColor(R.color.colorPrimary))
                .center(new LatLng(location.getLatitude(), location.getLongitude())).strokePattern(pattern);
        currentCircle.add(map.addCircle(circleOptions.radius(150)));
        currentCircle.add(map.addCircle(circleOptions.radius(250)));
        currentCircle.add(map.addCircle(circleOptions.radius(500)));
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    public void onCameraMove() {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLng northeast = bounds.northeast;
        LatLng southwest = bounds.southwest;

        new ShowStopsTask(dataBase, map, currentStations).execute(northeast, southwest);
    }


    private static class ShowStopsTask extends AsyncTask<LatLng, Integer, List<Station>> {
        HuberDataBase dataBase;
        GoogleMap map;
        Map<Integer, Station> currentStations;

        private ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations) {
            this.dataBase = dataBase;
            this.map = map;
            this.currentStations = currentStations;
        }

        @Override
        protected List<Station> doInBackground(LatLng... latLngs) {
            return dataBase.stationDao().getInBound(latLngs[0].longitude, latLngs[0].latitude, Math.abs(latLngs[1].longitude), Math.abs(latLngs[1].latitude));
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
        }
    }
}
