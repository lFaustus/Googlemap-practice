package com.example.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.google.playservices.placecomplete.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationProvider.LocationCallback {

    List<LatLng> latlnglist;
    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private LocationProvider mLocationProvider;
    private Marker trackingMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        mLocationProvider = new LocationProvider(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mMap.clear();

         latlnglist = new ArrayList<>();
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        latlnglist.add(latLng);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        Intent mIntent = getIntent();
        String name = mIntent.getStringExtra("Name");
        String address = mIntent.getStringExtra("Address");
        LatLng destinationLatLng = new LatLng(mIntent.getDoubleExtra("Lat",0.0),mIntent.getDoubleExtra("Lng",0.0));
        Location destinationLocation = new Location("");
        destinationLocation.setLatitude(destinationLatLng.latitude);
        destinationLocation.setLongitude(destinationLatLng.longitude);
        latlnglist.add(destinationLatLng);
        MarkerOptions currentlocation = new MarkerOptions()
                .position(latLng)
                .position(destinationLatLng);

        trackingMarker = mMap.addMarker(currentlocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        /*PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.addAll(latlnglist);
        mMap.addPolyline(options);*/
        Marker marker,marker2;
        MarkerOptions currentlocationPin = new MarkerOptions()
                .position(latLng)
                .title("I am here");
        mMap.addMarker(currentlocationPin);

        MarkerOptions destinationPin = new MarkerOptions()
                .position(destinationLatLng)
                .title("Destination - Distance: " + location.distanceTo(destinationLocation) / 1000 + " KM");
        marker2 = mMap.addMarker(destinationPin);

        marker2.showInfoWindow();

        startAnimation();


        /*MarkerOptions destination = new MarkerOptions()
                .position(destinationLatLng)
                .title("Destination!");*/


        //mMap.addMarker(destination);


    }

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        int EARTH_RADIUS_KM = 6371;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad)) * EARTH_RADIUS_KM;
    }
    int currentPt;
    private void startAnimation() {

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latlnglist.get(0), 16),
                5000,
                simpleAnimationCancelableCallback);

        currentPt = 0-1;
    }
    GoogleMap.CancelableCallback simpleAnimationCancelableCallback =
            new GoogleMap.CancelableCallback(){

                @Override
                public void onCancel() {
                }

                @Override
                public void onFinish() {

                    if(++currentPt < latlnglist.size()){

//					double heading = SphericalUtil.computeHeading(googleMap.getCameraPosition().target, markers.get(currentPt).getPosition());
//					System.out.println("Heading  = " + (float)heading);
//					float targetBearing = bearingBetweenLatLngs(googleMap.getCameraPosition().target, markers.get(currentPt).getPosition());
//					System.out.println("Bearing  = " + targetBearing);
//
                        LatLng targetLatLng = latlnglist.get(currentPt);

                        CameraPosition cameraPosition =
                                new CameraPosition.Builder()
                                        .target(targetLatLng)
                                        .tilt(currentPt<latlnglist.size()-1 ? 90 : 0)
                                                //.bearing((float)heading)
                                        .zoom(mMap.getCameraPosition().zoom)
                                        .build();


                        mMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                3000,
                                simpleAnimationCancelableCallback);

                       // highLightMarker(currentPt);

                    }
                }
            };

   /* private void highLightMarker(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker.showInfoWindow();
        //Utils.bounceMarker(googleMap, marker);
        this.selectedMarker=marker;
    }*/


}
