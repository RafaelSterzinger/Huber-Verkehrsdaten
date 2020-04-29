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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


import android.util.Log;
import android.view.View;

import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener {

    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 25;

    private GoogleMap map;
    private LocationManager location;
    private View mapView;
    private LatLng lastCameraPosition;

    private HuberDataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        map.setOnCameraMoveCanceledListener(this);
        // View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        // RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

// align location button with rlp
        /*


         */
    }

    @Override
    public void onLocationChanged(Location location) {
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

//        Context context = getApplicationContext();
//        CharSequence text = "ne:" + northeast + " sw:" + southwest;
//        int duration = Toast.LENGTH_SHORT;

//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();


            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        final LatLng northeast = bounds.northeast;
        final LatLng southwest = bounds.southwest;

        AsyncTask task = new ShowStops(northeast,southwest);
        task.execute();

    }


    @Override
    public void onCameraMoveCanceled() {
        System.out.println("Camera move stopped");
    }

    private class ShowStops extends AsyncTask{

        List<Station> stations = null;
        LatLng northeast;
        LatLng southwest;

        public ShowStops(LatLng northeast, LatLng southwest) {
            this.northeast = northeast;
            this.southwest = southwest;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            stations = dataBase.stationDao().getInBound(northeast.longitude, northeast.latitude, Math.abs(southwest.longitude), Math.abs(southwest.latitude));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            map.clear();
            for (Station st : stations) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(st.getLat(),st.getLon()));
                map.addMarker(markerOptions);
            }
        }
    }
}
