package com.example.huber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.SphericalUtil;
import com.mancj.materialsearchbar.MaterialSearchBar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Material recommends AppCompatActivity
public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener,
        NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 25;
    private static final float MAX_ZOOM_LEVEL = 15f;
    private static final float INITIAL_ZOOM_LEVEL = 17f;
    private static final int ACTIVITY_REQUEST_CODE_SETTINGS = 1;

    // must have same length
    private static final double[] distances = {150/4.0, 250/4.0, 500/4.0};
    private static final String[] distanceLabels = {"3'", "5'", "10'"};

    private GoogleMap map;
    private View mapView;
    private Map<Integer, Station> currentStations = new ConcurrentHashMap<>();
    private List<Circle> currentCircles = new ArrayList<>();
    private List<Marker> currentDistanceMarkers = new ArrayList<>();

    private MaterialButton favourites;
    private MaterialButton overview;

    private HuberDataBase dataBase;

    MaterialSearchBar searchBar;
    private DrawerLayout drawer;
    CustomSuggestionsAdapter customSuggestionsAdapter;

    // settings, favourites
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        mapView = mapFragment.getView();

        initializeSearchbarDrawer();

        dataBase = HuberDataBase.Companion.invoke(getApplicationContext());

        setupSharedPreferences();
        /* respective apply needed
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_preference_name), MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        sharedPreferencesEditor.putInt("insert", 1);
        sharedPreferencesEditor.apply();
         */
    }

    private void setupSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /*
            Activity1: startActivityForResult(intent, ACTIVITY_REQUEST_CODE_SETTINGS);
            Activity2: setResult(RESULT_OK);
                       finish();
            Activity1: onActivityResult()
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == ACTIVITY_REQUEST_CODE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                //updateDistanceCircles();
                Log.d("FIRST", "Value should be 2. Actual value: " + sharedPreferences.getInt("insert", -3));

                /*
                // get String data from Intent
                String returnString = data.getStringExtra(Intent.EXTRA_TEXT);

                // set text view with string
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(returnString);
                */
            }
        }
    }

    private void initializeSearchbarDrawer() {
        drawer = findViewById(R.id.drawer_layout);                                                  // opens the drawer onButtonClicked
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        searchBar.setCardViewElevation(10);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        customSuggestionsAdapter = new CustomSuggestionsAdapter(inflater);

        // TODO: check why this does not work
        searchBar.setMaxSuggestionCount(3);

        searchBar.setCustomSuggestionAdapter(customSuggestionsAdapter);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
                // send the entered text to our filter and let it manage everything
                customSuggestionsAdapter.getFilter().filter(searchBar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
    }

    @Override
    // returning true will select the Item in the Drawer!
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_map){
            drawer.closeDrawer(GravityCompat.START);
            //return true;
        } else if (id == R.id.nav_settings) {
            customSuggestionsAdapter.clearSuggestions();         // DO NOT REMOVE TODO: opening the other activity throws an error if the suggestions point to existing values!
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //startActivityForResult(intent, ACTIVITY_REQUEST_CODE_SETTINGS);
            //return true;
        } else if (id == R.id.nav_favourites){
            Intent intent = new Intent(this, DrawerItemActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_disturbance){
            Intent intent = new Intent(this, DrawerItemActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (enabled){
            customSuggestionsAdapter.setSuggestions(new ArrayList<>(currentStations.values()));
        } else {
            //customSuggestionsAdapter.clearSuggestions();
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        // TODO: select the first one in suggestions if it exits
    }

    @Override
    // SearchBar
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawer.openDrawer(GravityCompat.START);
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
        }
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

        initializeOnMapReady(location);
    }

    private void initializeOnMapReady(Location location) {
        overview = findViewById(R.id.overview);
        overview.setChecked(true);
        favourites = findViewById(R.id.favourites);

        // position the location Button
        // TODO: SearchBar height hard coded
        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on top right
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

            // Retrieve a dimensional for a particular resource ID. Unit conversions are based on the current DisplayMetrics associated with the resources.
            // so if you want exact dp value just as in xml just divide it with DisplayMetrics density
            int sB_margin = (int) (getResources().getDimension(R.dimen.searchBar_margin)); // / getResources().getDisplayMetrics().density);
            int sB_margin_top = sB_margin +
                    ((int) (getResources().getDimension(R.dimen.searchBar_height)) - (int) (getResources().getDimension(R.dimen.locationButton_height))) / 2;
            System.out.println(sB_margin_top);
            layoutParams.setMargins(0, sB_margin_top, sB_margin, 0);
        }

        if (location != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, INITIAL_ZOOM_LEVEL));
            setDistanceCircles(location);
        }
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

        CircleOptions circleOptions = new CircleOptions().strokeColor(getColor(R.color.colorPrimary))
                .center(latLng).strokeWidth(3);
        int walk_Speed = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.settings_key_walking_speed), "4"));

        for (int i = 0; i < distances.length; i++) {
            currentCircles.add(map.addCircle(circleOptions.radius(distances[i] * walk_Speed)));
            Marker distanceMarker = map.addMarker(new MarkerOptions().position(
                    SphericalUtil.computeOffset(latLng, distances[i] * walk_Speed, 45)).                 // computes the position of going 250m from latLng into direction 45°
                    icon(createPureTextIcon(distanceLabels[i])));                                   // calls a Method to create an icon for the Marker (in this case a Text Icon)
            currentDistanceMarkers.add(distanceMarker);
        }
    }

    // only update cirlces and markers
    private void updateDistanceCircles() {
        int walk_Speed = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.settings_key_walking_speed), "4"));
        if (currentCircles != null) {
            for (int i = 0; i < currentCircles.size(); i++) {
                currentCircles.get(i).setRadius(distances[i] * walk_Speed);
            }
        }
        if (currentDistanceMarkers != null) {
            for (int i = 0; i < currentDistanceMarkers.size(); i++) {
                //workaround to not have to save the location TODO: change
                currentDistanceMarkers.get(i).setPosition(SphericalUtil.computeOffset(currentCircles.get(0).getCenter(), distances[i] * walk_Speed, 45));
            }
        }
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
        if (searchBar.isSearchEnabled()){ searchBar.disableSearch();}

        new ShowStopsTask(dataBase, map, currentStations, this::updateView).execute(northeast, southwest);
    }

    public void getFavourites(View view) {
        if (favourites.isChecked()) {
            overview.setChecked(false);
        }
    }

    public void getOverview(View view) {
        if (overview.isChecked()) {
            favourites.setChecked(false);
        }
    }

    private void updateView() {
        runOnUiThread(() -> {
            LinearLayout scrollView = (LinearLayout) findViewById(R.id.scrollView);

            Map<Integer, View> currentEntriesInView = IntStream.range(0, scrollView.getChildCount())
                    .mapToObj(scrollView::getChildAt).collect(Collectors.toMap(View::getId, view -> view));

            Collection<Integer> toRemove = currentEntriesInView.keySet().stream()
                    .filter(station -> !currentStations.containsKey(station)).collect(Collectors.toList());

            Collection<Integer> toAdd = currentStations.keySet().stream()
                    .filter(station -> !currentEntriesInView.containsKey(station)).collect(Collectors.toList());

            toRemove.forEach(id -> ((ViewGroup) scrollView).removeView(currentEntriesInView.get(id)));

            toAdd.forEach(id -> {
                LayoutInflater inflater = LayoutInflater.from(this);
                Station station = currentStations.get(id);
                View view = inflater.inflate(R.layout.entry, scrollView, false);
                view.setId(Objects.requireNonNull(station).getUid());
                ((TextView) view.findViewById(R.id.station)).setText(station.getName());
                ((TextView) view.findViewById(R.id.minute)).setText("5'");
                scrollView.addView(view);
            });
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.settings_key_walking_speed))){
            updateDistanceCircles();
        }
    }

    private static class ShowStopsTask extends AsyncTask<LatLng, Integer, List<Station>> {
        private HuberDataBase dataBase;
        private GoogleMap map;
        private Map<Integer, Station> currentStations;
        private Runnable callback;

        private ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations) {
            this.dataBase = dataBase;
            this.map = map;
            this.currentStations = currentStations;
        }

        private ShowStopsTask(HuberDataBase dataBase, GoogleMap map, Map<Integer, Station> currentStations, Runnable callback) {
            this.dataBase = dataBase;
            this.map = map;
            this.currentStations = currentStations;
            this.callback = callback;
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
            if (callback != null) {
                callback.run();
            }
        }
    }
}
