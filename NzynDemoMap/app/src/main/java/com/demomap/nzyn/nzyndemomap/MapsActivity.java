package com.demomap.nzyn.nzyndemomap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.demomap.nzyn.nzyndemomap.model.Leg;
import com.demomap.nzyn.nzyndemomap.model.Route;
import com.demomap.nzyn.nzyndemomap.utils.Constants;
import com.demomap.nzyn.nzyndemomap.utils.PermissionUtils;
import com.demomap.nzyn.nzyndemomap.builder.RoutesBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.demomap.nzyn.nzyndemomap.utils.Constants.DEFAULT_FAST_LOCATION_REQUEST_TIME_INTERVAL;
import static com.demomap.nzyn.nzyndemomap.utils.Constants.DEFAULT_LINE_THICKNESS;
import static com.demomap.nzyn.nzyndemomap.utils.Constants.DEFAULT_LOCATION_REQUEST_TIME_INTERVAL;
import static com.demomap.nzyn.nzyndemomap.utils.Constants.DEFAULT_ZOOM_LEVEL;
import static com.demomap.nzyn.nzyndemomap.utils.Constants.REQUEST_CODE_ZOOM_INTO_CURRENT_LOCATION;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient
        .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ActivityCompat
        .OnRequestPermissionsResultCallback, RoutesBuilder.RoutesBuilderListener {
    private static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng mLatLng;
    private String startName;
    private String destinationName;
    private String mAvoid;
    private String mMode;
    private float mZoomLevel = Constants.DEFAULT_ZOOM_LEVEL;
    private boolean mLocationPermissionGranted;
    private EditText editTextStart;
    private EditText editTextDestination;
    List<Marker> allMarkers = new ArrayList<>();
    private CheckBox checkBoxFreeway;
    private CheckBox checkBoxShortest;
    private CheckBox checkBoxShowTraffic;
    /**
     * The formatted location address.
     */
    private String mAddressOutput;

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    /**
     * Represents a geographical location.
     */
    private Location mLastLocation;
    /**
     * Provides access to the Fused Location Provider API.
     */
//    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     */
    private boolean mAddressRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);
        editTextStart = (EditText) findViewById(R.id.editTextStart);
        editTextStart.addTextChangedListener(textWatcher);
        editTextStart.requestFocus();
        editTextDestination = (EditText) findViewById(R.id.editTextDestination);
        editTextDestination.addTextChangedListener(textWatcher);
        checkBoxFreeway = (CheckBox) findViewById(R.id.checkboxFreeway);
        checkBoxShowTraffic = (CheckBox) findViewById(R.id.checkboxTrafic);
        checkBoxShortest = (CheckBox) findViewById(R.id.checkboxShortest);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(editTextStart.getEditableText() == s){
                startName = s.toString();
            }else if(editTextDestination.getEditableText() == s){
                destinationName = s.toString();
            }
            processRoute();
        }
    };

    private void processRoute(){
        if(TextUtils.isEmpty(startName)){
            return;
        }
        if(TextUtils.isEmpty(destinationName)){
            return;
        }
        if(mMap != null) {
            getDirection();
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkboxFreeway:
                if (checked) {
                    mAvoid = "";
                }else{
                    mAvoid = "highways";
                }
                getDirection();
                break;
            case R.id.checkboxShortest:
                if (checked) {

                }else{

                }
                getDirection();
                break;
            case R.id.checkboxTrafic:
                if (checked) {
                    mMap.setTrafficEnabled(true);
                }else{
                    mMap.setTrafficEnabled(false);
                }
                break;
        }
    }

    public boolean getDirectionAvoidHighway(){
        mAvoid = "highways";
        return getDirection();
    }

    public boolean getDirectionForBicycling(){
        mMode = "bicycling";
        return getDirection();
    }

    public boolean getDirectionShortest(){
        return getDirection();
    }

    public boolean getDirection(){
        hideKeyboardFrom(this, editTextDestination);
        startName = editTextStart.getText().toString();
        destinationName = editTextDestination.getText().toString();
        if(TextUtils.isEmpty(startName)){
            Toast.makeText(this, "Please enter your origin name/address.", Toast.LENGTH_LONG).show();
            return false;
        }
        if(TextUtils.isEmpty(destinationName)){
            Toast.makeText(this, "Please enter your destination name/address.", Toast.LENGTH_LONG).show();
            return false;
        }

        mMap.clear();
        RoutesBuilder routesBuilder = new RoutesBuilder(this);
        try {
            routesBuilder.build(startName, destinationName, mAvoid, mMode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }



    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        Log.v(TAG, "onMapReady");
        mMap = map;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        zoomIntoDeviceLocation();
        if(!TextUtils.isEmpty(startName) && !TextUtils.isEmpty(destinationName)){
            getDirection();
        }
    }

    private void focusToAPoint(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private synchronized GoogleApiClient buildGoogleAPIClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int val) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "onLocationChanged - location = " + location.toString());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.v(TAG, "onRequestPermissionsResult - requestCode = " + requestCode);
        if (requestCode != REQUEST_CODE_ZOOM_INTO_CURRENT_LOCATION) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            Log.v(TAG, "Permission is granted");
            mLocationPermissionGranted = true;
            zoomIntoDeviceLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mLocationPermissionGranted = false;
        }
    }

    private void zoomIntoDeviceLocation() {
        Log.v(TAG, "zoomIntoDeviceLocation");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "onConnected no permissions");
            checkPermission(REQUEST_CODE_ZOOM_INTO_CURRENT_LOCATION);
            return;
        }
        Location currentDeviceLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (currentDeviceLocation != null) {
            mMap.clear();
            mLatLng = new LatLng(currentDeviceLocation.getLatitude(), currentDeviceLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mLatLng);
            markerOptions.title(getString(R.string.current_position_title));
            mMap.addMarker(markerOptions);
            focusToAPoint(mLatLng, DEFAULT_ZOOM_LEVEL);
            mMap.setMyLocationEnabled(true);

        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(DEFAULT_LOCATION_REQUEST_TIME_INTERVAL);
        mLocationRequest.setFastestInterval(DEFAULT_FAST_LOCATION_REQUEST_TIME_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener)
                this);

    }

    public LatLng getLatLngByAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng latLng = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();

            latLng = new LatLng(latitude, longtitude );

            Log.v(TAG, "My house latitude = " + latitude + ", longtitude = " + longtitude);
        } catch (IOException ex) {

            ex.printStackTrace();

        }

        return latLng;
    }

    public boolean start(String start){
        if(TextUtils.isEmpty(start)){
            return false;
        }
        editTextStart.setText(start);
        startName = start;
        return true;
    }

    public boolean goTo(String destination){
        if(TextUtils.isEmpty(destination)){
            return false;
        }
        destinationName = destination;
        editTextDestination.setText(destinationName);
        return true;
    }

    @Override
    public void onRouteBuilderSuccess(List<Route> routes){
        drawRoutes(routes);
    }
    @Override
    public void onRouteBuilderFailure(){

    }

    private void drawRoutes(List<Route> routes){
        if(routes == null){
            return;
        }
        if(routes.size() == 0){
            return;
        }
        List<Marker> originMarkers = new ArrayList<>();
        List<Marker> destinationMarkers = new ArrayList<>();
        List<Polyline> polylinePaths = new ArrayList<>();

        allMarkers.clear();

        if(checkBoxShortest.isChecked()){
            int shortest = findShortestRoute(routes);
            Route route = routes.get(shortest);
            drawRoute(route, originMarkers, destinationMarkers, polylinePaths, R.color.colorBlue);

        }else{
            for (int i=routes.size()-1; i>=0; i--) {
                Route route = routes.get(i);
                if(i == 0){
                    drawRoute(route, originMarkers, destinationMarkers, polylinePaths, R.color.colorBlue);
                }else{
                    drawRoute(route, originMarkers, destinationMarkers, polylinePaths, R.color.colorLightGray);
                }

            }
        }


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : allMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 200; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.moveCamera(cu);

    }

    private void drawRoute(Route route, List<Marker> originMarkers, List<Marker> destinationMarkers, List<Polyline>
            polylinePaths, int color){

        Leg leg = route.getLegs().get(0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(leg.getStartLocation().getLatLng(), 16));
        ((TextView) findViewById(R.id.textViewDuration)).setText(leg.getDuration().getText());



        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
                .title(leg.getStartAddress())
                .position(leg.getStartLocation().getLatLng())));
        allMarkers.addAll(originMarkers);

        destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end))
                .title(leg.getEndAddress())
                .position(leg.getEndLocation().getLatLng())));
        allMarkers.addAll(destinationMarkers);


        int width = getResources().getInteger(R.integer.defaultLineThickness);

        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(ContextCompat.getColor(this, color)).
                width(width);

        for (int j = 0; j < route.getPoints().size(); j++)
            polylineOptions.add(route.getPoints().get(j));

        polylinePaths.add(mMap.addPolyline(polylineOptions));
    }



    public void checkPermission(int reqCode) {
        Log.v(TAG, "checkPermission");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is not granted");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO:
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.v(TAG, "Should show the explanation of the needs of the permission");
                buildAlertMessageNoGps(reqCode);
            } else {
                // No explanation needed, we can request the permission.
                Log.v(TAG, "Ignore explanation, go ahead and request the permission");
                requestLocationPermission(reqCode);
            }
        }


        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.v(TAG, "GPS provider is not enabled");
            buildAlertMessageNoGps(reqCode);
        }
    }

    private void buildAlertMessageNoGps(final int reqCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Log.v(TAG, "Request for permission");
                        requestLocationPermission(reqCode);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void requestLocationPermission(int tag){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                tag);
    }

    public void portrait(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void landscape(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    public void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void zoomIn(){
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOut(){
        mMap.animateCamera(CameraUpdateFactory.zoomOut());
    }

    private int findShortestRoute(List<Route> routes){
        int shortest = routes.get(0).getLegs().get(0).getDistance().getValue();
        int index = 0;
        for(int i=1; i<routes.size(); i++){
            int distance = routes.get(i).getLegs().get(0).getDistance().getValue();
            if( distance < shortest){
                shortest = distance;
                index = i;
            }
        }
        return index;
    }


    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    /**
     * Gets the address for the last known location.
     */
    @SuppressWarnings("MissingPermission")
//    private void getAddress() {
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location == null) {
//                            Log.w(TAG, "onSuccess:null");
//                            return;
//                        }
//
//                        mLastLocation = location;
//
//                        // Determine whether a Geocoder is available.
//                        if (!Geocoder.isPresent()) {
//                            showSnackbar(getString(R.string.no_geocoder_available));
//                            return;
//                        }
//
//                        // If the user pressed the fetch address button before we had the location,
//                        // this will be set to true indicating that we should kick off the intent
//                        // service after fetching the location.
//                        if (mAddressRequested) {
//                            startIntentService();
//                        }
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "getLastLocation:onFailure", e);
//                    }
//                });
//    }

    /**
     * Updates the address in the UI.
     */
    private void displayAddressOutput() {
        editTextStart.setText(mAddressOutput);
    }


    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
//                showToast(getString(R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
//            updateUIWidgets();
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString("start", startName);
        outState.putString("destination", destinationName);
        outState.putBoolean("checkbox_traffic", checkBoxShowTraffic.isChecked());
        outState.putBoolean("checkbox_freeway", checkBoxFreeway.isChecked());
        outState.putBoolean("checkbox_shortest", checkBoxShortest.isChecked());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        startName = savedInstanceState.getString("start");
        destinationName = savedInstanceState.getString("destination");
        editTextStart.setText(startName);
        editTextDestination.setText(destinationName);
        checkBoxShowTraffic.setChecked(savedInstanceState.getBoolean("checkbox_traffic"));
        checkBoxFreeway.setChecked(savedInstanceState.getBoolean("checkbox_freeway"));
        checkBoxShortest.setChecked(savedInstanceState.getBoolean("checkbox_shortest"));
    }
}
