package com.example.huber;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.example.huber.activity.DisturbancesActivity;
import com.example.huber.activity.DrawerItemActivity;
import com.example.huber.activity.SettingsActivity;
import com.example.huber.alarm.AlarmManager;
import com.example.huber.alarm.AlarmReceiver;
import com.example.huber.alarm.CustomAlarmDialog;
import com.example.huber.alarm.CustomSnoozeDialog;
import com.example.huber.database.HuberDataBase;
import com.example.huber.databinding.DirectionEntryBinding;
import com.example.huber.databinding.EntryBinding;
import com.example.huber.entity.Station;
import com.example.huber.live.entity.data.Departure;
import com.example.huber.live.entity.data.Monitor;
import com.example.huber.task.FilterStopsTask;
import com.example.huber.task.MoveCameraTask;
import com.example.huber.task.ShowStopsTask;
import com.example.huber.task.UpdateDBStationFavoriteTask;
import com.example.huber.util.BitmapDescriptorIconCreator;
import com.example.huber.util.CustomSuggestionsAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.SphericalUtil;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener,
        NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String ACTIVITY_NAME = "MAIN ACTIVITY";

    public static final String RLB = "DIRECTION_ID";
    public static final String STATION_NAME = "STOP_NAME";
    public static final String DIRECTION_NAME = "DIRECTION_NAME";
    public static final String STATION_UID = "STATION_UID";

    public static final String CAMERA_LAT = "LAT";
    public static final String CAMERA_LON = "LON";
    public static final String CAMERA_ZOOM = "ZOOM";
    public static final String CURRENT_SELECTION = "CURRENT_SELECTION";
    public static final int ACTIVITY_RESULT_CODE_FAVORITE_ONSUGGESTIONCLICK = 11;
    private static final int ACTIVITY_REQUEST_CODE_FAVORITE = 1;
    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 10;
    private static final float MAX_ZOOM_LEVEL = 13f;
    private static final float INITIAL_ZOOM_LEVEL = 16f;
    // must have same length
    private static final double[] distances = {150 / 3.0, 250 / 3.0, 500 / 3.0};
    private static final String[] distanceLabels = {"3'", "5'", "10'"};
    private final List<Circle> currentCircles = new ArrayList<>();
    private final List<Marker> currentDistanceMarkers = new ArrayList<>();
    private BroadcastReceiver alarmReceiver;
    private Timer timer;
    private int walkSpeed = 4;
    private Map<Integer, Station> currentStations = new ConcurrentHashMap<>();
    private GoogleMap map;
    private View mapView;
    private Location location;
    private int currentSelection = -1;

    private MaterialButton favorites;
    private MaterialButton overview;

    private HuberDataBase dataBase;

    private SlidingUpPanelLayout slideUp;
    private DrawerLayout drawer;
    private MaterialSearchBar searchBar;
    private CustomSuggestionsAdapter suggestionsAdapter;

    private CustomSnoozeDialog snoozeDialog;

    // settings, favorites
    private SharedPreferences sharedPreferences;
    private Polyline arrow;

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
    }

    private void setupSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    private void initializeSearchBar() {
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        searchBar.findViewById(R.id.mt_clear).setOnClickListener(v -> searchBar.disableSearch());

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        suggestionsAdapter = new CustomSuggestionsAdapter(inflater);
        searchBar.setCustomSuggestionAdapter(suggestionsAdapter);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 0) {
                    suggestionsAdapter.setSuggestions(new ArrayList<>(currentStations.values()));
                    if (searchBar.isSearchEnabled()) {
                        searchBar.showSuggestionsList();
                    }
                } else {
                    new FilterStopsTask(dataBase, suggestionsAdapter, () -> searchBar.showSuggestionsList()).execute(charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_map) {
            drawer.closeDrawer(GravityCompat.START);
            //returning true will select the Item in the Drawer!
            //return true;
        } else if (id == R.id.nav_settings) {
            suggestionsAdapter.clearSuggestions();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            //startActivityForResult(intent, ACTIVITY_REQUEST_CODE_SETTINGS);
            //return true;
        } else if (id == R.id.nav_favorites) {
            suggestionsAdapter.clearSuggestions();
            Intent intent = new Intent(this, DrawerItemActivity.class);
            intent.putExtra("type", "Favoriten");
            startActivityForResult(intent, ACTIVITY_REQUEST_CODE_FAVORITE);
        } else if (id == R.id.nav_disturbance) {
            suggestionsAdapter.clearSuggestions();
            Intent intent = new Intent(this, DisturbancesActivity.class);
            startActivity(intent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
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
        if (requestCode == ACTIVITY_REQUEST_CODE_FAVORITE) {
            Log.d("AFTER FAVOURITE", "onActivityResult: refreshing favorites");
            updateOverview();
            //getFavourites(findViewById(R.id.favorites));
            /*if (favorites.isChecked()) {
                Log.d(favorites.isChecked() + "" + overview.isChecked(), "onActivityResult: if");
                //favorites.setChecked(true);
                favorites.performClick();                      // problem with setting favorite.isChecked to true
                Log.d(favorites.isChecked() + "" + overview.isChecked(), "onActivityResult: if");

                //getFavourites(findViewById(R.id.favorites));                                       // reload stations after closing favorites Drawer Item
            } else {
                Log.d(favorites.isChecked() + "" + overview.isChecked(), "onActivityResult:else ");
                //overview.setChecked(true);
                overview.performClick();
                //getOverview(findViewById(R.id.overview));
            }*/
            overview.performClick();                // makes sure to reload all stations
            slideUp.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            if (resultCode == ACTIVITY_RESULT_CODE_FAVORITE_ONSUGGESTIONCLICK) {
                onSuggestionClick(Integer.parseInt(Objects.requireNonNull(data.getDataString())));


                /*
                // get String data from Intent
                String returnString = data.getStringExtra(Intent.EXTRA_TEXT);

                // set text view with string
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(returnString);
                */
            }
            Log.d(favorites.isChecked() + "" + overview.isChecked(), "onActivityResult: end");
        }
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (enabled) {
            suggestionsAdapter.setSuggestions(new ArrayList<>(currentStations.values()));
        }
        slideUp.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        Log.d(ACTIVITY_NAME, "DO NOT USE THE ENTER KEY");
        List<Station> suggestions = suggestionsAdapter.getSuggestions();
        if (suggestions != null && suggestions.size() > 0) {
            View view = searchBar.findViewById(suggestions.get(0).getUid());
            onSuggestionClick(view);
        }
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
        if (requestCode == LOCATION_PERMISSION) {
            if (Arrays.stream(grantResults).anyMatch(x -> x == -1)) {
                finish();
            } else {
                createLocationManager();
            }
        }
    }

    private Location createLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
        } else {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, DISTANCE_UPDATE, this);
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setCompassEnabled(false);
                map.getUiSettings().setTiltGesturesEnabled(false);
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        location = createLocationManager();

        map.setOnCameraIdleListener(this);
        map.setOnCameraMoveStartedListener(this);
        map.setMinZoomPreference(MAX_ZOOM_LEVEL);
        map.setOnMyLocationButtonClickListener(() -> {
            slideUp.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return false;
        });
        map.setOnMapClickListener(latLng -> {
            if (arrow != null) {
                arrow.remove();
                arrow = null;
            }
            currentSelection = -1;
            // Call updateView() to reorder opened overview
            updateOverview();
        });
        map.setOnMarkerClickListener(marker -> {
            if (arrow != null) {
                arrow.remove();
                arrow = null;
            }

            int clickedStationID = currentStations.values().stream().
                    filter(station -> station.getName().equals(marker.getTitle())).findFirst().
                    map(Station::getUid).orElse(-1);
            currentSelection = clickedStationID == currentSelection ? -1 : clickedStationID;

            slideUp.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return false;
        });
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        googleMap.setMapStyle(mapStyleOptions);

        if (sharedPreferences.contains(CAMERA_LAT) && sharedPreferences.contains(CAMERA_LON) && sharedPreferences.contains(CAMERA_ZOOM)) {
            CameraPosition lastPosition = new CameraPosition.Builder()
                    .target(new LatLng(sharedPreferences.getFloat(CAMERA_LAT, 0), sharedPreferences.getFloat(CAMERA_LON, 0)))
                    .zoom(sharedPreferences.getFloat(CAMERA_ZOOM, 0)).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(lastPosition));
            if (location != null) {
                afterMovePositionOrChangeDistance();
            }
            currentSelection = sharedPreferences.getInt(CURRENT_SELECTION, currentSelection);
        } else if (location != null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, INITIAL_ZOOM_LEVEL));
            afterMovePositionOrChangeDistance();
        } else {
            // If last GPS-location is unknown camera is moved to Vienna
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.208176, 16.373819), INITIAL_ZOOM_LEVEL));
        }
        positionLocateButton();
    }

    private void positionLocateButton() {
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
            int sBMargin = (int) (getResources().getDimension(R.dimen.searchBar_margin)); // / getResources().getDisplayMetrics().density);
            int sBMarginTop = sBMargin +
                    ((int) (getResources().getDimension(R.dimen.searchBar_height)) - (int) (getResources().getDimension(R.dimen.locationButton_height))) / 2;
            layoutParams.setMargins(0, sBMarginTop, sBMargin, 0);
        }
    }


    /**
     * Sets the distance circles and also calculates the distance to each station
     */
    private void afterMovePositionOrChangeDistance() {
        if (location != null) {
            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

            if (currentCircles != null) {
                currentCircles.forEach(Circle::remove);
                currentCircles.clear();
            }
            if (currentDistanceMarkers != null) {
                currentDistanceMarkers.forEach(Marker::remove);
                currentDistanceMarkers.clear();
            }

            CircleOptions circleOptions = new CircleOptions().strokeColor(getColor(R.color.colorPrimary))
                    .center(currentPosition).strokeWidth(3);
            walkSpeed = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.settings_key_walking_speed), "4"));

            for (int i = 0; i < distances.length; i++) {
                Objects.requireNonNull(currentCircles).add(map.addCircle(circleOptions.radius(distances[i] * walkSpeed)));
                Marker distanceMarker = map.addMarker(new MarkerOptions().position(
                        // computes the position of going 250m from latLng into direction 45°
                        SphericalUtil.computeOffset(currentPosition, distances[i] * walkSpeed + 17, 45)).
                        // calls a Method to create an icon for the Marker (in this case a Text Icon)
                                icon(BitmapDescriptorIconCreator.createPureTextIcon(distanceLabels[i], getResources())));
                Objects.requireNonNull(currentDistanceMarkers).add(distanceMarker);
            }

            currentStations.values().forEach(station -> station.setDistance(currentPosition, walkSpeed));
        }
    }

    // ATTENTION: if calling manually, set favorites.setChecked(true) beforehand
    public void getFavourites(@SuppressWarnings("unused") View view) {
        if (favorites.isChecked()) {
            overview.setChecked(false);
            slideUp.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
        currentStations.values().forEach(station -> Objects.requireNonNull(station.getMarker()).remove());
        currentStations = new ConcurrentHashMap<>();
        LatLng northeast;
        LatLng southwest;
        if (location != null) {
            northeast = new LatLng(location.getLatitude(), location.getLongitude());
            southwest = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;              // TODO refractor with getOverview() and onCameraIdle() to trun into one method
            northeast = bounds.northeast;
            southwest = bounds.southwest;
        }
        new ShowStopsTask(dataBase, map, currentStations, this::updateOverview, walkSpeed, location, true).execute(northeast, southwest);   // favorites ordered by distance to user if location exists and screen otherwise
    }

    public void getOverview(@SuppressWarnings("unused") View view) {
        if (overview.isChecked()) {
            favorites.setChecked(false);
            slideUp.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
        currentStations.values().forEach(station -> Objects.requireNonNull(station.getMarker()).remove());
        currentStations = new ConcurrentHashMap<>();
        onCameraIdle();
    }

    public void setAlarm(View view) {
        Log.d(ACTIVITY_NAME, "Setting an alarm");
        Integer stationID = ((ViewGroup) view.getParent().getParent().getParent()).getId();
        Station station = currentStations.get(stationID);
        CustomAlarmDialog dialog = new CustomAlarmDialog(this, Objects.requireNonNull(station));
        dialog.show();
    }

    private void updateOverview() {
        Log.d(ACTIVITY_NAME, "Updating overview");
        LinearLayout scrollView = findViewById(R.id.scrollView);
        LayoutInflater inflater = LayoutInflater.from(this);

        runOnUiThread(() -> {
            scrollView.removeAllViews();

            // CurrentSelection gets placed on top
            Station currentSelectionStation = currentStations.get(this.currentSelection);
            if (currentSelectionStation != null) {
                addEntryToView(scrollView, inflater, currentSelectionStation);
                Objects.requireNonNull(currentSelectionStation.getMarker()).showInfoWindow();
            }

            currentStations.values().stream().filter(station -> station.getUid() != this.currentSelection).sorted((station1, station2) -> Double.compare(station1.getDistanceKm(), station2.getDistanceKm())).
                    forEach(station -> addEntryToView(scrollView, inflater, station));
        });

    }

    private void addEntryToView(LinearLayout scrollView, LayoutInflater inflater, Station station) {
        EntryBinding binding = DataBindingUtil.inflate(inflater, R.layout.entry, scrollView, false);
        binding.setStationVar(station);

        View view = binding.getRoot();
        int stationID = Objects.requireNonNull(station).getUid();
        view.setId(stationID);
        TextView heading = view.findViewById(R.id.station);
        heading.setId(stationID);
        view.findViewById(R.id.favour_true).setId(stationID);           // setting it on the encapsulating R.id.favour_both destroys the formatting for some reason
        view.findViewById(R.id.favour_false).setId(stationID);

        station.requestLiveData((List<Monitor> monitors) -> {
            TableLayout table = view.findViewById(R.id.directions);

            for (Monitor monitor : monitors) {
                DirectionEntryBinding tableEntryBinding = DataBindingUtil.inflate(inflater, R.layout.direction_entry, table, false);
                tableEntryBinding.setMonitor(monitor);

                List<Departure> departures = monitor.getLines().get(0).getDepartures().getDeparture();
                int departureSize = departures.size();
                if (departureSize >= 1 && departures.get(0).getDepartureTime().getCountdown() != null) {
                    tableEntryBinding.setFirstTrain(departures.get(0).getDepartureTime().getCountdown());
                    tableEntryBinding.setSecondTrain(departureSize >= 2 ? departures.get(1).getDepartureTime().getCountdown() : null);
                    tableEntryBinding.setWalkTime(station.getDistanceMinutes() + (station.getDistanceHours() * 60));
                    table.addView(tableEntryBinding.getRoot());
                }
            }
        });

        scrollView.addView(view);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.settings_key_walking_speed))) {
            afterMovePositionOrChangeDistance();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        afterMovePositionOrChangeDistance();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(STATION_UID)) {
            NotificationManager mgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) {
                mgr.cancel(0);
            }
            Log.d(ACTIVITY_NAME, "Entering activity from notification");
            triggerSnooze(intent, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(ACTIVITY_NAME, "Receiving alarm");
                triggerSnooze(intent, false);
                abortBroadcast();
            }
        };
        IntentFilter filter = new IntentFilter(AlarmManager.ALARM_EVENT);
        registerReceiver(alarmReceiver, filter);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(ACTIVITY_NAME, "Updating departures");
                currentStations.values().forEach(station -> {
                    if (station.getMonitor() != null && station.getMonitor().size() > 0) {
                        station.setMonitor(null);
                    }
                });
                updateOverview();
            }
        }, 30 * 1000, 30 * 1000);
    }

    private void triggerSnooze(Intent intent, boolean fromNotification) {
        long rlb = intent.getLongExtra(MainActivity.RLB, -1);
        String direction = intent.getStringExtra(MainActivity.DIRECTION_NAME);
        int stationUID = intent.getIntExtra(MainActivity.STATION_UID, -1);

        Ringtone r = AlarmReceiver.getR();
        Vibrator v = AlarmReceiver.getV();
        try {
            if (r != null) {
                r.stop();
            }
            if (v != null) {
                v.cancel();
            }
        } catch (Throwable t) {
            Log.d("ALARM RECEIVER", "Stopping Alarm");
        }


        new Thread(() -> {
            Station station = dataBase.stationDao().getStationWithUID(stationUID);
            if (location != null) {
                station.setDistance(new LatLng(location.getLatitude(), location.getLongitude()), walkSpeed);
            }
            station.requestLiveData((monitors -> {
                CustomSnoozeDialog dialog = new CustomSnoozeDialog(rlb, station, direction, fromNotification);
                dialog.show(getSupportFragmentManager().beginTransaction(), "SnoozeDialog");
                snoozeDialog = dialog;
            }));
        }).start();
    }

    @Override
    protected void onPause() {
        searchBar.clearSuggestions();
        unregisterReceiver(alarmReceiver);
        timer.cancel();

        if (map != null) {
            CameraPosition currentCameraPosition = map.getCameraPosition();
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putFloat(CAMERA_LAT, (float) currentCameraPosition.target.latitude);
            sharedPreferencesEditor.putFloat(CAMERA_LON, (float) currentCameraPosition.target.longitude);
            sharedPreferencesEditor.putFloat(CAMERA_ZOOM, currentCameraPosition.zoom);
            sharedPreferencesEditor.putInt(CURRENT_SELECTION, currentSelection);
            sharedPreferencesEditor.apply();
        }

        if (snoozeDialog != null) {
            snoozeDialog.dismiss();
        }

        super.onPause();
    }

    @Override
    public void onCameraIdle() {
        if (!favorites.isChecked()) {
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            LatLng northeast = bounds.northeast;
            LatLng southwest = bounds.southwest;
            new ShowStopsTask(dataBase, map, currentStations, this::updateOverview, walkSpeed, location, false).execute(northeast, southwest);
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (searchBar.isSearchEnabled()) {
            searchBar.disableSearch();
        }
    }

    @Override
    protected void onStart() {
        overview = findViewById(R.id.overview);
        overview.setChecked(true);
        favorites = findViewById(R.id.favorites);
        slideUp = findViewById(R.id.sliding_up_panel);
        slideUp.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if ((previousState == SlidingUpPanelLayout.PanelState.COLLAPSED && newState == SlidingUpPanelLayout.PanelState.DRAGGING)
                        || newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    findViewById(R.id.button_group_overview_favorites).setVisibility(View.VISIBLE);
                    findViewById(R.id.sliding_up_panel_handle).setVisibility(View.INVISIBLE);
                } else if /*((previousState == SlidingUpPanelLayout.PanelState.EXPANDED && newState == SlidingUpPanelLayout.PanelState.DRAGGING)
                        ||*/ (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    findViewById(R.id.button_group_overview_favorites).setVisibility(View.INVISIBLE);
                    findViewById(R.id.sliding_up_panel_handle).setVisibility(View.VISIBLE);
                }
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeSearchBar();
        super.onStart();
    }

    // Arrow gets added when searching or clicking on a suggestion
    public void onSuggestionClick(View view) {
        onSuggestionClick(view.getId());
    }

    public void onSuggestionClick(int id) {
        if (arrow != null) {
            arrow.remove();
            arrow = null;
        }
        currentSelection = id;          // must be set here since we call this method through the favorites Drawer Item
        if (slideUp.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            slideUp.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        new MoveCameraTask(dataBase, map, (currentStation) -> {
            if (location != null) {
                arrow = map.addPolyline(
                        new PolylineOptions().add(
                                Objects.requireNonNull(currentStation).getLatLng(),
                                new LatLng(location.getLatitude(), location.getLongitude())));
                arrow.setStartCap(new CustomCap(
                        BitmapDescriptorIconCreator.bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_arrow_upward_black_24dp), 10));
                arrow.setEndCap(new RoundCap());
            }
        }).execute(currentSelection);
    }

    public void onFavouriteClick(View view) {
        onFavouriteClick(view.getId());
    }

    public void onFavouriteClick(int id) {
        Station currentStation = currentStations.get(id);
        new UpdateDBStationFavoriteTask(dataBase).execute(currentStation);
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
}
