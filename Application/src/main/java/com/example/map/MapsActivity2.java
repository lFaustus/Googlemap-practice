package com.example.map;

import android.os.Bundle;

import com.example.google.playservices.placecomplete.R;
import com.google.android.gms.maps.MapView;

/**
 * Created by Fluxi on 11/4/2015.
 */
public class MapsActivity2 extends MapsActivity {
    MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapView = (MapView)findViewById(R.id.mapView);

    }
}
