package com.example.linux.polygonmaps;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.maps.android.SphericalUtil;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    private EditText editText;
    private Button buttonStartStop;
    private Button buttonClear;
    private boolean drawLines;
    private PolylineOptions polylineOptions;
    private PolygonOptions polygon;
    private Polyline polyline;
    private String polygonMarkers = "polygonMarkers";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        System.out.println("Hallo");

        // Restore preferences
        settings = getPreferences(MODE_PRIVATE);
        editor = settings.edit();



        editText = (EditText) findViewById(R.id.editText);
        buttonClear = (Button) findViewById(R.id.clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                editor.clear();
                editor.apply();
            }
        });
        buttonStartStop = (Button) findViewById(R.id.StartStopPolygon);
        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawLines){
                    if(polylineOptions.getPoints().size() > 1){
                        // Draw polygon
                        polyline.remove();
                        mMap.addPolygon(polygon.strokeColor(Color.RED).fillColor(0x4FFF0000));

                        //Area Berechnung: http://stackoverflow.com/questions/28838287/calculate-the-area-of-a-polygon-drawn-on-google-maps-in-an-android-application
                        double area = SphericalUtil.computeArea(polylineOptions.getPoints());

                        LatLng center = calcCentroid();
                        mMap.addMarker(new MarkerOptions().position(center).title(area + ""));
                    }
                    buttonStartStop.setText("Start Polygon");
                    polylineOptions = null;
                    polygon = null;
                    drawLines = false;
                }else{
                    // Start Drawing Lines
                    buttonStartStop.setText("End Polygon");
                    drawLines = true;
                    polylineOptions = new PolylineOptions();
                    polygon = new PolygonOptions();
                }
            }
        });




    }

    //https://en.wikipedia.org/wiki/Centroid
    private LatLng calcCentroid() {
        List<LatLng> points = polygon.getPoints();

        double x = 0, y = 0;

        for (int i = 0; i < points.size(); i++) {
            x += points.get(i).latitude;
            y += points.get(i).longitude;
        }

        x /= points.size();
        y /= points.size();
        return new LatLng(x,y);
    }

    /*Ursprüngliche Implementierung nach der Wikipedia Formel:
     https://de.wikipedia.org/wiki/Polygon
      hat leider nicht funktioniert

    private double calcArea() {
        List<LatLng> points = polygon.getPoints();
        double area = 0;
        double x,x2,y,y2 = 0;


        for (int i = 0; i < points.size() - 1; i++){
            x = convert_x(points.get(i));
            x2 = convert_x(points.get(i + 1));
            y = convert_y(points.get(i));
            y2 = convert_y(points.get(i + 1));
            area += (x * y2) - (x2 * y);
        }
        return Math.abs(area) * 0.5;
    }

    // http://stackoverflow.com/questions/1185408/converting-from-longitude-latitude-to-cartesian-coordinates
    private double convert_x(LatLng latLng) {
        return (6371 * cos(latLng.latitude) * cos(latLng.longitude));
    }

    private double convert_y(LatLng latLng) {
        return (6371 * cos(latLng.latitude) * sin(latLng.longitude));
    } */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(settings.contains(polygonMarkers)){
            Set<String> markers = settings.getStringSet(polygonMarkers, new HashSet<String>());
            for (String marker : markers){
                Log.i("Marker", marker);
                String[] tmp = marker.split(",");
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[2]))).title(tmp[0]));
            }

        }
        // Add a marker in Sydney and move the camera
        LatLng weimar = new LatLng(50.980557, 11.332340);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String title = editText.getText().toString();
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
                mMap.addMarker(markerOptions);
                Set<String> set = settings.getStringSet(polygonMarkers, new HashSet<String>());
                Log.i("Set", set.toString());
                set.add(markerOptions.getTitle() + "," + markerOptions.getPosition().latitude + "," + markerOptions.getPosition().longitude);
                editor.clear();
                editor.putStringSet(polygonMarkers, set);
                editor.apply();
                if(drawLines){
                    polygon.add(latLng);
                    if (polyline != null){
                        polyline.remove();
                    }
                    polylineOptions.add(latLng);
                    polyline = mMap.addPolyline(polylineOptions);


                }
            }
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(weimar, 18));


    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context

    }


}
