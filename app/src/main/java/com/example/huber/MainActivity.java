package com.example.huber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.SphericalUtil;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Material recommends AppCompatActivity
//TODO: AppCompatActivity has a toolbar (FragmentActivity does not); remove View.OnClickListener
public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ALARM_ID = "DIRECTION_ID";
    public static final String STOP_NAME = "STOP_NAME";
    public static final String DIRECTION_NAME = "DIRECTION_NAME";

    private BroadcastReceiver alarmReceiver;

    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 0;
    private static final float MAX_ZOOM_LEVEL = 13f;
    private static final float INITIAL_ZOOM_LEVEL = 16f;
    private static final int ACTIVITY_REQUEST_CODE_SETTINGS = 1;

    // must have same length
    private static final double[] distances = {150 / 4.0, 250 / 4.0, 500 / 4.0};
    private static final String[] distanceLabels = {"3'", "5'", "10'"};

    private Map<Integer, Station> currentStations = new ConcurrentHashMap<>();
    private GoogleMap map;
    private View mapView;
    private List<Circle> currentCircles = new ArrayList<>();
    private List<Marker> currentDistanceMarkers = new ArrayList<>();

    private MaterialButton favourites;
    private MaterialButton overview;

    private HuberDataBase dataBase;

    private MaterialSearchBar searchBar;
    private DrawerLayout drawer;
    private CustomSuggestionsAdapter customSuggestionsAdapter;

    // settings, favourites
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        mapView = mapFragment.getView();
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
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
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

    @Override
    protected void onStart() {
        super.onStart();
        initializeSearchbarDrawer();
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

        searchBar.findViewById(R.id.mt_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.disableSearch();
            }
        });

        searchBar.setCustomSuggestionAdapter(customSuggestionsAdapter);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TextChangeListener","beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
                //new FilterStopsTask();

                customSuggestionsAdapter.getFilter().filter(searchBar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("TextChangeListener","afterTextChanged");
            }
        });
    }

    @Override
    // returning true will select the Item in the Drawer!
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_map) {
            drawer.closeDrawer(GravityCompat.START);
            //return true;
        } else if (id == R.id.nav_settings) {
            customSuggestionsAdapter.clearSuggestions();         // DO NOT REMOVE TODO: opening the other activity throws an error if the suggestions point to existing values!
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //startActivityForResult(intent, ACTIVITY_REQUEST_CODE_SETTINGS);
            //return true;
        } else if (id == R.id.nav_favourites) {
            Intent intent = new Intent(this, DrawerItemActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_disturbance) {
            Intent intent = new Intent(this, DrawerItemActivity.class);
            startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (enabled) {
            customSuggestionsAdapter.setSuggestions(new ArrayList<>(currentStations.values()));
        } else {
            // customSuggestionsAdapter.clearSuggestions();
            // searchBar.hideSuggestionsList();
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        // TODO: select the first one in suggestions if it exits
    }

    @Override
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setOnCameraIdleListener(this);
        map.setMinZoomPreference(MAX_ZOOM_LEVEL);
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        googleMap.setMapStyle(mapStyleOptions);
        initialize(createLocationManager());
    }

    private void positionLocateButton() {
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
    }

    private void initialize(Location location) {
        overview = findViewById(R.id.overview);
        overview.setChecked(true);
        favourites = findViewById(R.id.favourites);

        positionLocateButton();

        if (location != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, INITIAL_ZOOM_LEVEL));
            setDistanceCircles(location);
        } else {
            // If last GPS-location is unknown camera is moved to Vienna
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.208176, 16.373819), INITIAL_ZOOM_LEVEL));
        }
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
        int walkSpeed = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.settings_key_walking_speed), "4"));

        for (int i = 0; i < distances.length; i++) {
            currentCircles.add(map.addCircle(circleOptions.radius(distances[i] * walkSpeed)));
            Marker distanceMarker = map.addMarker(new MarkerOptions().position(
                    // computes the position of going 250m from latLng into direction 45°
                    SphericalUtil.computeOffset(latLng, distances[i] * walkSpeed + 15, 45)).
                    // calls a Method to create an icon for the Marker (in this case a Text Icon)
                            icon(PureTextIconCreator.createPureTextIcon(distanceLabels[i], getResources())));
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

    public void getFavourites(View view) {
        if (favourites.isChecked()) {
            overview.setChecked(false);
        }
        currentStations = new ConcurrentHashMap<>();
        updateView();
    }

    public void getOverview(View view) {
        if (overview.isChecked()) {
            favourites.setChecked(false);
        }
        onCameraIdle();
    }

    public void setAlarm(View view) {
        Integer stationID = ((ViewGroup) view.getParent().getParent().getParent()).getId();
        Station station = currentStations.get(stationID);
        LayoutInflater inflater = LayoutInflater.from(this);
        // Necessary to access values from time picker and spinner at onClick
        @SuppressLint("InflateParams") View config = inflater.inflate(R.layout.alarm_config, null);
        TimePicker tp = config.findViewById(R.id.time_picker);
        Spinner sp = config.findViewById(R.id.direction_picker);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_notifications_black_24dp)
                .setTitle(Objects.requireNonNull(station).getName())
                .setView(config)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, tp.getHour());
                    c.set(Calendar.MINUTE, tp.getMinute());
                    c.set(Calendar.SECOND, 0);

                    //System.out.println(sp.getSelectedItem());
                    AlarmManager.setAlarm(MainActivity.this, 1000, station.getName(), sp.getSelectedItem().toString(), c);
                    Toast.makeText(MainActivity.this, "Development Alarm erstellt", Toast.LENGTH_LONG).show();
                });
        builder.show();
    }

    public void setSnooze(long directionID, String station, String direction) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Objects.requireNonNull(v).vibrate(AlarmManager.DEFAULT_VIBRATION);
        } else {
            Objects.requireNonNull(v).vibrate(AlarmManager.DEFAULT_VIBRATION_LENGTH);
        }

        int[] placeholder = new int[]{1, 3, 12};

        LayoutInflater inflater = LayoutInflater.from(this);
        // Necessary to set values beforehand
        @SuppressLint("InflateParams") View config = inflater.inflate(R.layout.snooze_config, null);
        ((TextView) config.findViewById(R.id.direction)).setText(direction);
        ((TextView) config.findViewById(R.id.walk)).setText("7'");
        ((TextView) config.findViewById(R.id.direction_arrival)).setText("9'");
        final ListView list = config.findViewById(R.id.next_conncetions);
        list.setAdapter(new ArrayAdapter<>(this, R.layout.single_choice_layout, Arrays.stream(placeholder).mapToObj(entry -> entry + "'").toArray(String[]::new)));
        /*
        final int[] po = {-1};
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                po[0] = position;
            }
        });
         */

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_notifications_black_24dp)
                .setTitle(station)
                .setView(config)
                .setNegativeButton(R.string.snooze, (dialog, which) -> {
                    v.cancel();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.add(Calendar.MINUTE, 1);
                    AlarmManager.setAlarm(this, 1000, station, direction, calendar);
                    Toast.makeText(MainActivity.this, "Development Snooze for " + 1 + " Minuten", Toast.LENGTH_LONG).show();
                })
                .setPositiveButton(R.string.cancle_alarm, (dialog, which) -> v.cancel());
        builder.show();
    }

    private void updateView() {
        //TODO only call method when slide panel is up or being pulled up for performance
        LinearLayout scrollView = findViewById(R.id.scrollView);
        LayoutInflater inflater = LayoutInflater.from(this);

        Map<Integer, View> currentEntriesInView = IntStream.range(0, scrollView.getChildCount())
                .mapToObj(scrollView::getChildAt).collect(Collectors.toMap(View::getId, view -> view));
        Collection<Integer> toRemove = currentEntriesInView.keySet().stream()
                .filter(station -> !currentStations.containsKey(station)).collect(Collectors.toList());
        Collection<Integer> toAdd = currentStations.keySet().stream()
                .filter(station -> !currentEntriesInView.containsKey(station)).collect(Collectors.toList());

        runOnUiThread(() -> {
            toRemove.forEach(id -> scrollView.removeView(currentEntriesInView.get(id)));
            toAdd.forEach(id -> {
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
        if (key.equals(getResources().getString(R.string.settings_key_walking_speed))) {
            updateDistanceCircles();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        setDistanceCircles(location);
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
    protected void onPause() {
        super.onPause();
        unregisterReceiver(alarmReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long directionID = intent.getLongExtra(MainActivity.ALARM_ID, -1);
                String station = intent.getStringExtra(MainActivity.STOP_NAME);
                String direction = intent.getStringExtra(MainActivity.DIRECTION_NAME);

                setSnooze(directionID, station, direction);
                abortBroadcast();
            }
        };
        IntentFilter filter = new IntentFilter(AlarmManager.ALARM_EVENT);
        registerReceiver(alarmReceiver, filter);
    }

    @Override
    public void onCameraIdle() {
        if (!favourites.isChecked()) {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            LatLng northeast = bounds.northeast;
            LatLng southwest = bounds.southwest;
            new ShowStopsTask(dataBase, map, currentStations, this::updateView).execute(northeast, southwest);
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (searchBar.isSearchEnabled()) {
            searchBar.disableSearch();
        }
    }
}
