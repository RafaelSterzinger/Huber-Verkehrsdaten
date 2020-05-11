package com.example.huber;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.maps.android.SphericalUtil;
import com.mancj.materialsearchbar.MaterialSearchBar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TODO: AppCompatActivity has a toolbar (FragmentActivity does not); remove View.OnClickListener
public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnCameraMoveListener {

    private static final int LOCATION_PERMISSION = 69;
    private static final int DISTANCE_UPDATE = 25;
    private static final float MAX_ZOOM_LEVEL = 15f;
    private static final float INITIAL_ZOOM_LEVEL = 17f;

    private GoogleMap map;
    private View mapView;
    private Map<Integer, Station> currentStations = new ConcurrentHashMap<>();
    private List<Circle> currentCircles = new ArrayList<>();
    private List<Marker> currentDistanceMarkers = new ArrayList<>();

    private MaterialButton favourites;
    private MaterialButton overview;

    private HuberDataBase dataBase;

    MaterialSearchBar searchBar;
    CustomSuggestionsAdapter customSuggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        mapView = mapFragment.getView();

        addStationsToSuggestion();

        dataBase = HuberDataBase.Companion.invoke(getApplicationContext());
    }

    private void addStationsToSuggestion() {
        searchBar = findViewById(R.id.searchBar);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        customSuggestionsAdapter = new CustomSuggestionsAdapter(inflater);

        // TODO: check why this does not work
        searchBar.setMaxSuggestionCount(3);
        //searchBar.setHint("Suche Haltestelle");

        List<Station> suggestions = new ArrayList<>();//
        if (currentStations != null && !currentStations.isEmpty()) {
            suggestions.addAll(currentStations.values());
        } else {
            for (int i = 1; i < 10; i++) {
                suggestions.add(new Station(i, i, "Test" + i, "Test" + i, i, i, i));
            }
        }

        customSuggestionsAdapter.setSuggestions(suggestions);


        // TODO: call setSuggestions on UpdateView, but only set the CustomSuggestionAdapter and the TextSearchListener once
        searchBar.setCustomSuggestionAdapter(customSuggestionsAdapter);

        /* TODO
                Cancel/X Button in searchbar
                Back Button/Arrow
                Drawer

                see examples https://github.com/mancj/MaterialSearchBar
                Listener for On Click
                    https://camposha.info/android-material-toolbar-searchbar-search-filter-listview/
                    https://camposha.info/android-recyclerview-materialsearchbar-search-filter/
                Listener for search
         */
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
        map.setOnCameraMoveListener(this);
        map.setMinZoomPreference(MAX_ZOOM_LEVEL);
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        googleMap.setMapStyle(mapStyleOptions);

        Location location = createLocationManager();

        initializeWithoutLocation();
        if (location != null) {
            initialize(location);
        }

        // align location button with rlp
        // View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        // RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
    }

    //TODO set position to vienna in combine method
    private void initializeWithoutLocation() {
        overview = findViewById(R.id.overview);
        overview.setChecked(true);
        favourites = findViewById(R.id.favourites);
        // manual Testing on Guntramsdorf TODO: remove
        // setDistanceCirlces(new LatLng( 48.0485, 16.3071));

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

    }

    private void initialize(Location location) {
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

    public void createAlarm(View view) {
        Integer station_ID = ((ViewGroup) view.getParent().getParent().getParent()).getId();
        Station station = currentStations.get(station_ID);
        LayoutInflater inflater = LayoutInflater.from(this);
        // Necessary to access values from time picker and spinner at onClick
        @SuppressLint("InflateParams") View config = inflater.inflate(R.layout.alarm_config, null);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_notifications_black_24dp)
                .setTitle(Objects.requireNonNull(station).getName())
                .setView(config)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TimePicker tp = config.findViewById(R.id.time_picker);
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, tp.getHour());
                        c.set(Calendar.MINUTE, tp.getMinute());
                        c.set(Calendar.SECOND, 0);

                        Spinner sp = config.findViewById(R.id.direction_picker);
                        System.out.println(sp.getSelectedItem());
                        startAlarm(c);
                    }
                });
        builder.show();
    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        Objects.requireNonNull(alarmManager).setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    private void updateView() {
        runOnUiThread(() -> {
            LinearLayout scrollView = findViewById(R.id.scrollView);
            LayoutInflater inflater = LayoutInflater.from(this);

            Map<Integer, View> currentEntriesInView = IntStream.range(0, scrollView.getChildCount())
                    .mapToObj(scrollView::getChildAt).collect(Collectors.toMap(View::getId, view -> view));
            Collection<Integer> toRemove = currentEntriesInView.keySet().stream()
                    .filter(station -> !currentStations.containsKey(station)).collect(Collectors.toList());
            Collection<Integer> toAdd = currentStations.keySet().stream()
                    .filter(station -> !currentEntriesInView.containsKey(station)).collect(Collectors.toList());

            toRemove.forEach(id -> ((ViewGroup) scrollView).removeView(currentEntriesInView.get(id)));
            toAdd.forEach(id -> {
                Station station = currentStations.get(id);
                View view = inflater.inflate(R.layout.entry, scrollView, false);
                view.setId(Objects.requireNonNull(station).getUid());
                ((TextView) view.findViewById(R.id.station)).setText(station.getName());
                ((TextView) view.findViewById(R.id.minute)).setText("5'");
                scrollView.addView(view);
            });

            // update SearchBar Suggestions based on visible stations
            // TODO: Fix BUG: when the searchBar is opened and we move the map, the suggestions do not get updated until we close the searchBar
            //          easiest fix: close the SearchBar when moving the map/updating the view -> call the On X/Close Click Listener (not yet implemented)
            customSuggestionsAdapter.setSuggestions(new ArrayList<Station>(currentStations.values()));
        });
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
