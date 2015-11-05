package com.example.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.google.playservices.placecomplete.R;
import com.example.volley.MyApplication;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.api.client.http.GenericUrl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fluxi on 11/4/2015.
 */
public class MapsActivity2 extends FragmentActivity implements LocationProvider.LocationCallback {

    private GoogleMap googleMap;
    private LocationProvider mLocationProvider;
    private Marker trackingMarker;

    private List<Marker> markers = new ArrayList<Marker>();

    private final Handler mHandler = new Handler();

    private Marker selectedMarker;
    private Animator animator = new Animator();
    private static final String PLACES_DIRECTIONS = "http://maps.googleapis.com/maps/api/directions/json";
    private Directions mDirections;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();


    }

    @Override
    protected void onStart() {
        super.onStart();
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
        animator.stopAnimation();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
        MenuInflater mMenu = getMenuInflater();
        menu.clear();
        mMenu.inflate(R.menu.animating_menu,menu);
        return true;
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.animating_menu, menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (item.getItemId() == R.id.action_bar_remove_location) {
            removeSelectedMarker();
        } else if (item.getItemId() == R.id.action_bar_add_default_locations) {;
            //addDefaultLocations();
        } else*/ if (item.getItemId() == R.id.action_bar_start_animation) {
            animator.startAnimation(true);
        } else if (item.getItemId() == R.id.action_bar_stop_animation) {
            animator.stopAnimation();
        } else if (item.getItemId() == R.id.action_bar_clear_locations) {
            clearMarkers();
        } else if (item.getItemId() == R.id.action_bar_toggle_style) {;
            //toggleStyle();
        }
        return true;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    public void handleNewLocation(Location location) {
        clearMarkers();
        Intent mIntent = getIntent();
        String name = mIntent.getStringExtra("Name");
        String address = mIntent.getStringExtra("Address");
        //addMarkerToMap(new LatLng(location.getLatitude(),location.getLongitude()));
        //addMarkerToMap(new LatLng(mIntent.getDoubleExtra("Lat", 0.0), mIntent.getDoubleExtra("Lng", 0.0)));

        setPlacesDirections(new LatLng(location.getLatitude(),location.getLongitude()),new LatLng(mIntent.getDoubleExtra("Lat", 0.0), mIntent.getDoubleExtra("Lng", 0.0)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

       // Log.e("location changed",new LatLng(location.getLatitude(),location.getLongitude()).toString());
        //animator.startAnimation(true);

    }

    public void setPlacesDirections(LatLng origin,LatLng destination) {

        GenericUrl genericUrl = new GenericUrl(PLACES_DIRECTIONS);
        genericUrl.put("origin",origin.latitude+","+origin.longitude);
        genericUrl.put("destination",destination.latitude+","+destination.longitude);
        genericUrl.put("sensor",false);
        genericUrl.put("mode", "driving");
        genericUrl.put("alternatives", true);
        JsonObjectRequest mJSONOBjectRequest = new JsonObjectRequest(Request.Method.GET, genericUrl.toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("URL",genericUrl.toString());
                try {

                    JSONArray mArray = response.getJSONArray("routes");
                    ArrayList<Directions> mDirectionsList = new ArrayList<>();
                    Directions mDirection;
                    Log.e("route size",mArray.length()+""  );
                    final int size = mArray.length();
                    for(int i = 0; i < size ; i++)
                    {
                        //JSONArray mLegsArray = mArray.getJSONArray(i);
                        JSONObject mObject = mArray.getJSONObject(i);
                        JSONArray mLegsArray = mObject.getJSONArray("legs");
                        Log.e("legs array size",mLegsArray.length()+"");
                        final int size2 = mLegsArray.length();
                        JSONObject mObject2 = mLegsArray.getJSONObject(0);
                        JSONArray mStepsArray = mObject2.getJSONArray("steps");
                        mDirection = new Directions();
                        String end_address = mObject2.getString("end_address"),
                                start_address = mObject2.getString("start_address");
                        JSONObject mOverviewPolylineObject = mObject.getJSONObject("overview_polyline");
                        mDirection.setOverviewPolyline(mOverviewPolylineObject.getString("points"));
                        mDirection.setEndAddress(end_address);
                        mDirection.setStartAddress(start_address);
                        for(int l = 0; l < mStepsArray.length(); l++)
                        {

                            JSONObject mObject3 = mStepsArray.getJSONObject(i);
                            Log.e("mobject3 size",mObject3.length()+"");
                            // JSONObject mDistanceObject = mObject3.getJSONObject("distance");
                            JSONObject mDistanceObject = mObject3.getJSONObject("distance"),
                                    mDurationObject = mObject3.getJSONObject("duration"),
                                    mEndLocationObject = mObject3.getJSONObject("end_location"),
                                    mStartLocationObject = mObject3.getJSONObject("start_location");
                                    //mPolylinePointsObject = mObject3.getJSONObject("polyline");

                            mDirection.setDistance(mDistanceObject.getString("text"));
                            mDirection.setDuration(mDurationObject.getString("text"));
                            mDirection.setEndLocation(mEndLocationObject.getDouble("lat"), mEndLocationObject.getDouble("lng"));
                            mDirection.setStartLocation(mStartLocationObject.getDouble("lat"), mStartLocationObject.getDouble("lng"));
                            //mDirection.setOverviewPolyline(mPolylinePointsObject.getString("points"));

                            mDirection.setTravelMode(mObject3.getString("travel_mode"));
                        }
                        /*for(int k = 0 ; k <size2 ; k++)
                        {

                            JSONObject mObject2 = mLegsArray.getJSONObject(i);
                           // Log.e("mobject2",mObject2.toString()+"");
                            //Log.e("mobject2 size",mObject2.length()+"");

                            JSONArray mStepsArray = mObject2.getJSONArray("steps");
                             mDirection = new Directions();
                            String end_address = mObject2.getString("end_address"),
                                    start_address = mObject2.getString("start_address");
                            mDirection.setEndAddress(end_address);
                            mDirection.setStartAddress(start_address);
                            for(int l = 0; l < mStepsArray.length(); l++)
                            {

                                JSONObject mObject3 = mStepsArray.getJSONObject(i);
                                Log.e("mobject3 size",mObject3.length()+"");
                               // JSONObject mDistanceObject = mObject3.getJSONObject("distance");
                                JSONObject mDistanceObject = mObject3.getJSONObject("distance"),
                                           mDurationObject = mObject3.getJSONObject("duration"),
                                           mEndLocationObject = mObject3.getJSONObject("end_location"),
                                           mStartLocationObject = mObject3.getJSONObject("start_location"),
                                           mPolylinePointsObject = mObject3.getJSONObject("polyline");

                                mDirection.setDistance(mDistanceObject.getString("text"));
                                mDirection.setDuration(mDurationObject.getString("text"));
                                mDirection.setEndLocation(mEndLocationObject.getDouble("lat"), mEndLocationObject.getDouble("lng"));
                                mDirection.setStartLocation(mStartLocationObject.getDouble("lat"), mStartLocationObject.getDouble("lng"));
                                mDirection.setOverviewPolyline(mPolylinePointsObject.getString("points"));
                                mDirection.setTravelMode(mObject3.getString("travel_mode"));
                            }
                           // mDirections = mDirection;
                        }*/
                        mDirections = mDirection;
                        mDirectionsList.add(mDirection);
                    }
                    for(int v = 0 ; v < mDirectionsList.size(); v ++)
                        markers.addAll(decodePoly(mDirections.getOverview_polyline(),mDirections));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        MyApplication.Volley().getRequestQueue().add(mJSONOBjectRequest);
        //System.out.println(mDirections.toString());
    }



    class Directions{

        private String distance;
        private String duration;
        private String endAddress;
        private LatLng endLocation;
        private String startAddress;
        private LatLng startLocation;
        private String overview_polyline;
        private String travel_mode;

        Directions(){}

        public void setDistance(String distance)
        {
            this.distance = distance;
        }

        public void setDuration(String duration)
        {
            this.duration = duration;
        }

        public void setEndAddress(String endAddress)
        {
            this.endAddress = endAddress;
        }

        public void setEndLocation(double lat,double lng)
        {
            this.endLocation = new LatLng(lat,lng);
        }

        public void setStartAddress(String startAddress)
        {
            this.startAddress = startAddress;
        }

        public void setStartLocation(double lat,double lng)
        {
            this.startLocation = new LatLng(lat,lng);
        }

        public void setOverviewPolyline(String overview_polyline)
        {
            this.overview_polyline = overview_polyline;
        }

        public void setTravelMode(String travel_mode)
        {
            this.travel_mode = travel_mode;
        }

        public String getDistance() {
            return distance;
        }

        public String getDuration() {
            return duration;
        }

        public String getEndAddress() {
            return endAddress;
        }

        public LatLng getEndLocation() {
            return endLocation;
        }

        public String getStartAddress() {
            return startAddress;
        }

        public LatLng getStartLocation() {
            return startLocation;
        }

        public String getOverview_polyline() {
            return overview_polyline;
        }

        public String getTravel_mode() {
            return travel_mode;
        }

        @Override
        public String toString() {
            return "Distance: " + distance +" Duration: " + duration +" EndAddress: "+ endAddress + " EndLocation: " + endLocation.toString()
                    + " startAddress: " + startAddress + " startLocation: " + startLocation.toString() + " polyline points: " +overview_polyline
                    + " Travel Mode: " + travel_mode;
        }
    }



    public class Animator implements Runnable {

        private static final int ANIMATE_SPEEED = 1500;
        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;

        private final Interpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;

        float tilt = 90;
        float zoom = 15.5f;
        boolean upward=true;

        long start = SystemClock.uptimeMillis();

        LatLng endLatLng = null;
        LatLng beginLatLng = null;

        boolean showPolyline = false;

        private Marker trackingMarker;

        public void reset() {
            resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        public void stop() {
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);

        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;

            highLightMarker(0);

            if (showPolyLine) {
                polyLine = initializePolyLine();
            }

            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = markers.get(0).getPosition();
            LatLng secondPos = markers.get(1).getPosition();

            setupCameraPositionForMovement(markerPos, secondPos);

        }

        private void setupCameraPositionForMovement(LatLng markerPos,
                                                    LatLng secondPos) {

            float bearing = bearingBetweenLatLngs(markerPos,secondPos);

            trackingMarker = googleMap.addMarker(new MarkerOptions().position(markerPos)
                    .title("title")
                    .snippet("snippet"));

            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(trackingMarker.getPosition())
                            .bearing(bearing + BEARING_OFFSET)
                            .tilt(tilt)
                            .zoom(googleMap.getCameraPosition().zoom >= 16 ? googleMap.getCameraPosition().zoom : 16)
                            .build();
            //navigateToPoint(trackingMarker.getPosition(),0,cameraPosition.bearing,15,true);
            googleMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    }
            );
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();


        private Polyline initializePolyLine() {
            //polyLinePoints = new ArrayList<LatLng>();
            rectOptions.add(markers.get(0).getPosition());
            return googleMap.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {

            List<LatLng> points = polyLine.getPoints();
            /*LatLng p = new LatLng((((double) latLng.latitude / 1E5)),
                    (((double) latLng.longitude / 1E5)));*/
            points.add(latLng);
            polyLine.setPoints(points);
            polyLine.setGeodesic(true);
        }



        public void stopAnimation() {
            animator.stop();
        }

        public void startAnimation(boolean showPolyLine) {
            if (markers.size()>=2) {
                animator.initialize(showPolyLine);
            }
        }


        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float)elapsed/ANIMATE_SPEEED);

//			LatLng endLatLng = getEndLatLng();
//			LatLng beginLatLng = getBeginLatLng();

            double lat = t * endLatLng.latitude + (1-t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1-t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);
            /*trackingMarker.setTitle("Estimated Time of Arrival");

            trackingMarker.setSnippet(String.valueOf(TimeUnit.MILLISECONDS.toSeconds((long) t)));
            trackingMarker.showInfoWindow();*/

            if (showPolyline) {
               // Log.e("showpolyline",String.valueOf(convertLatLngToLocation(newPosition).getTime())+"");

              //  navigateToPoint(trackingMarker.getPosition(), 0, 0, 15, true);
                updatePolyLine(newPosition);


                //navigateToPoint(newPosition, true);

            }

            // It's not possible to move the marker + center it through a cameraposition update while another camerapostioning was already happening.
            //navigateToPoint(newPosition,tilt,bearing,currentZoom,false);
            //navigateToPoint(newPosition,false);

            if (t< 1) {
                //Log.e("t<1","t<1");
                mHandler.postDelayed(this, 16);
            } else {

                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                //if (currentIndex<markers.size()-1) {
                if (currentIndex<markers.size()-2) {

                    currentIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();


                    start = SystemClock.uptimeMillis();

                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();

                    float bearingL = bearingBetweenLatLngs(begin, end);

                    highLightMarker(currentIndex);

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(end) // changed this...
                                    .bearing(bearingL  + BEARING_OFFSET)
                                    .tilt(tilt)
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .build();

                    //navigateToPoint(trackingMarker.getPosition(),0,cameraPosition.bearing,15,true);
                    googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    //LatLng begin = getBeginLatLng();
                    //LatLng end = getEndLatLng();
                    //float bearingL = bearingBetweenLatLngs(begin, end);
                    currentIndex++;
                    highLightMarker(currentIndex);
                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(trackingMarker.getPosition()) // changed this...
                                    //.bearing(bearingL  + BEARING_OFFSET)
                                    //.tilt(tilt)
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .build();
                    //navigateToPoint(end,tilt,bearingL+BEARING_OFFSET,googleMap.getCameraPosition().zoom,false);
                    navigateToPoint(newPosition,false,cameraPosition);
                    stopAnimation();
                }

            }
        }




        private LatLng getEndLatLng() {
            return markers.get(currentIndex+1).getPosition();
        }

        private LatLng getBeginLatLng() {
            return markers.get(currentIndex).getPosition();
        }

        private void adjustCameraPosition() {
            //System.out.println("tilt = " + tilt);
            //System.out.println("upward = " + upward);
            //System.out.println("zoom = " + zoom);
            if (upward) {

                if (tilt<90) {
                    tilt ++;
                    zoom-=0.01f;
                } else {
                    upward=false;
                }

            } else {
                if (tilt>0) {
                    tilt --;
                    zoom+=0.01f;
                } else {
                    upward=true;
                }
            }
        }
    };



    private List<Marker> decodePoly(String encoded,Directions mDirections) {

        List<Marker> poly = new ArrayList<Marker>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        int counter = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            //googleMap.addMarker(new MarkerOptions().position(p).visible(false));
            //poly.add(p);
            Marker marker =googleMap.addMarker(new MarkerOptions().position(p));
            if(counter == 0)
            {
               marker.setTitle("I'm Here");
               marker.showInfoWindow();
            }
            else
                marker.setVisible(false);


            poly.add(marker);
            counter ++;
        }
        poly.get(poly.size()-1).setVisible(true);
        poly.get(poly.size()-1).setTitle("Destination Distance: " + mDirections.getDistance());
        poly.get(poly.size()-1).setSnippet("Estimated Time Of Arrival: " + mDirections.getDuration());
        poly.get(poly.size()-1).showInfoWindow();

       /* Marker m = poly.get(0);
        m.setTitle("I am here");
        m = poly.get(poly.size()-1);
        m.setTitle("Destination " + markers.size());
        m.setSnippet("Distance - " + distance);*/

        return poly;
    }
    /**
     * Allows us to navigate to a certain point.
     */
    public void navigateToPoint(LatLng latLng,float tilt, float bearing, float zoom,boolean animate) {
        CameraPosition position =
                new CameraPosition.Builder().target(latLng)
                        .zoom(zoom)
                        .bearing(bearing)
                        .tilt(tilt)
                        .build();

        changeCameraPosition(position, animate);

    }

    public void navigateToPoint(LatLng latLng, boolean animate,@Nullable CameraPosition mposition) {
        CameraPosition position;
        if(mposition == null)
            position = new CameraPosition.Builder().target(latLng).build();
        else
            position = mposition;
        changeCameraPosition(position, animate);
    }

    private void changeCameraPosition(CameraPosition cameraPosition, boolean animate) {
       // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cameraPosition.target, 15);
         CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);

        if (animate) {
            //googleMap.animateCamera(cameraUpdate,1500,null);
            googleMap.animateCamera(cameraUpdate,1500,null);
        } else {
           // cameraUpdate = CameraUpdateFactory.newLatLngZoom(cameraPosition.target, 15);
            googleMap.moveCamera(cameraUpdate);
        }

    }


    /*private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }*/


    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private float bearingBetweenLatLngs(LatLng begin,LatLng end) {
        Location beginL= convertLatLngToLocation(begin);
        Location endL= convertLatLngToLocation(end);

        return beginL.bearingTo(endL);
    }

    /**
     * Adds a marker to the map.
     */
    public void addMarkerToMap(LatLng latLng) {
        Marker marker;

        if(markers.size() == 0) {
             marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title("I am here")
                    .snippet("I am here"));
        }
        else{
            Location location = convertLatLngToLocation(markers.get(markers.size()-1).getPosition());
            Location destination = convertLatLngToLocation(latLng);

            marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                    .title("Destination " + markers.size())
                    .snippet("Distance  " + location.distanceTo(destination)/1000 +"KM"));
        }
        markers.add(marker);

    }

    /**
     * Clears all markers from the map.
     */
    public void clearMarkers() {
        googleMap.clear();
        markers.clear();
    }

    /**
     * Remove the currently selected marker.
     */
    public void removeSelectedMarker() {
        this.markers.remove(this.selectedMarker);
        this.selectedMarker.remove();
    }

    /**
     * Highlight the marker by index.
     */
    private void highLightMarker(int index) {
        highLightMarker(markers.get(index));
    }

    /**
     * Highlight the marker by marker.
     */
    private void highLightMarker(Marker marker) {

		/*
		for (Marker foundMarker : this.markers) {
			if (!foundMarker.equals(marker)) {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			} else {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				foundMarker.showInfoWindow();
			}
		}
		*/
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        //marker.showInfoWindow();

        //Utils.bounceMarker(googleMap, marker);

        this.selectedMarker = marker;
        navigateToPoint(selectedMarker.getPosition(),true,null);
    }

    private void resetMarkers() {
        for (Marker marker : this.markers) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }
}
