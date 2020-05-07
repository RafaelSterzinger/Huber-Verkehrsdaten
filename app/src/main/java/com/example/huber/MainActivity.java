package com.example.huber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.maps.android.SphericalUtil;


import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener {

    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 25;
    private static final float MAX_ZOOM_LEVEL = 13.5f;
    private static final float INITIAL_ZOOM_LEVEL = 15f;

    private GoogleMap map;
    private Map<Integer, Station> currentStations = new ConcurrentHashMap<>();
    private List<Circle> currentCircles = new ArrayList<>();
    private List<Marker> currentDistanceMarkers = new ArrayList<>();

    private MaterialButton favourites;
    private MaterialButton overview;

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

        dataBase = HuberDataBase.Companion.invoke(getApplicationContext());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                createLocationManager();
        }
    }

    private Location createLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
        } else {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, DISTANCE_UPDATE, this);
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        return null;
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
        map.setMinZoomPreference(MAX_ZOOM_LEVEL);
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        googleMap.setMapStyle(mapStyleOptions);

        Location location = createLocationManager();

        if (location != null) {
            initialize(location);
        }

        // align location button with rlp
        // View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        // RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
    }

    private void initialize(Location location) {
        overview = findViewById(R.id.overview);
        overview.setChecked(true);
        favourites = findViewById(R.id.favourites);
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, INITIAL_ZOOM_LEVEL));
        setDistanceCircles(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        setDistanceCircles(location);
    }

    private void setDistanceCircles(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentCircles != null) {
            currentCircles.forEach(Circle::remove);
            currentCircles.clear();
        }
        if (currentDistanceMarkers != null) {
            currentDistanceMarkers.forEach(Marker::remove);
            currentDistanceMarkers.clear();
        }

        // must have same length
        int[] distances = {150, 250, 500};
        String[] distanceLabels = {"3'", "5'", "10'"};

        //List<PatternItem> pattern = Collections.<PatternItem>singletonList(new Dot());
        CircleOptions circleOptions = new CircleOptions().strokeColor(getColor(R.color.colorPrimary))
                .center(latLng).strokeWidth(3);

        for (int i = 0; i < distances.length; i++) {
            currentCircles.add(map.addCircle(circleOptions.radius(distances[i])));
            Marker distanceMarker = map.addMarker(new MarkerOptions().position(
                    SphericalUtil.computeOffset(latLng, distances[i], 45)).                 // computes the position of going 250m from latLng into direction 45°
                    icon(createPureTextIcon(distanceLabels[i])));                                   // calls a Method to create an icon for the Marker (in this case a Text Icon)
            currentDistanceMarkers.add(distanceMarker);
        }

        /*
        //set Icon style
        /*
        //IconGenerator iconGen = new IconGenerator(this);
        //iconGen.setColor(Color.RED);
        //iconGen.setTextAppearance(R.style.);
        //Bitmap bm = iconGen.makeIcon("10'");
        //map.addMarker(new MarkerOptions().position(latLng).title("10'")).setIcon(BitmapDescriptorFactory.fromBitmap(bm));        // bzw .icon mit BitmapDescriptor


        //map.addMarker(new MarkerOptions().position(SphericalUtil.computeOffset(latLng, 150, 45))).setIcon(createPureTextIcon("5'"));
        //map.addMarker(new MarkerOptions().position(latLng).title("5'")).showInfoWindow();         // bzw .icon mit BitmapDescriptor
        */
    }

    // returns a BitmapDescriptor that can be used with setIcon() of a Marker
    // https://stackoverflow.com/questions/25544370/google-maps-api-for-android-v2-how-to-add-text-with-no-background
    public BitmapDescriptor createPureTextIcon(String text) {
        Paint textPaint = new Paint(); // Adapt to your needs

        // TODO: size should be relative to screen after zooming out/not shown at all
        int spSize = 17;
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spSize, getResources().getDisplayMetrics());
        textPaint.setTextSize(scaledSizeInPixels);

        float textWidth = textPaint.measureText(text);
        float textHeight = textPaint.getTextSize();
        int width = (int) (textWidth);
        int height = (int) (textHeight);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        canvas.translate(0, height);

        // For development only:
        // Set a background in order to see the
        // full size and positioning of the bitmap.
        // Remove that for a fully transparent icon.
        //canvas.drawColor(Color.LTGRAY);

        canvas.drawText(text, 0, 0, textPaint);
        return BitmapDescriptorFactory.fromBitmap(image);
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

    public void getFavourites(View view) {
        if (favourites.isChecked()) {
            overview.setChecked(false);
        }
    }

    public void getOverview(View view) {
        if (overview.isChecked()){
            favourites.setChecked(false);
        }
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
