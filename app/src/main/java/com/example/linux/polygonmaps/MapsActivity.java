package com.example.linux.polygonmaps;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SharedPreferences settings;
    private Button buttonStartStop;
    private Button buttonClear;
    private boolean drawLines;
    private PolylineOptions polylineOptions;
    private PolygonOptions polygon;
    private Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Restore preferences
        settings = getPreferences(MODE_PRIVATE);
        buttonClear = (Button) findViewById(R.id.clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
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
                        mMap.addPolygon(polygon.strokeColor(Color.RED).fillColor(Color.RED));
                        double area = calcArea();
                        LatLng center = calcCentroid(area);
                        mMap.addMarker(new MarkerOptions().position(center).title(area + ""));
                    }
                    buttonStartStop.setText("Start Polygon");
                    polylineOptions = null;
                    polygon = null;
                    drawLines = false;
                }else{
                    // Start Drawing Lines
                    buttonStartStop.setText("Stop Polygon");
                    drawLines = true;
                    polylineOptions = new PolylineOptions();
                    polygon = new PolygonOptions();
                }
            }
        });




    }

    private LatLng calcCentroid(double area) {
        List<LatLng> points = polygon.getPoints();
        double lat = 0;
        double lng = 0;
        double x,x2,y,y2 = 0;

        for (int i = 0; i < points.size()-1; i++){
            x = convert_x(points.get(i));
            x2 = convert_x(points.get(i + 1));
            y = convert_y(points.get(i));
            y2 = convert_y(points.get(i + 1));
            lat += (x + x2) * (x * y2 - x2 * y);
            lng += (y + y2) * (x * y2 - x2 * y);
        }
        double fac = 1 /(6 * area);
        lat *= fac;
        lng *= fac;

        //double lat2 = acos((lng) / (6371000 * sin(lng)));

        return new LatLng(lat, lng);
    }

    private double calcArea() {
        List<LatLng> points = polygon.getPoints();
        double area = 0;
        double x,x2,y,y2 = 0;
        double R = 6371000;

        for (int i = 0; i < points.size()-1; i++){
            x = convert_x(points.get(i));
            x2 = convert_x(points.get(i + 1));
            y = convert_y(points.get(i));
            y2 = convert_y(points.get(i + 1));
            area += (x * y2) - (x2 * y);
        }
        return area * 0.5;
    }

    private double convert_x(LatLng latLng) {
        return (6371000 * cos(latLng.latitude) * cos(latLng.longitude));
    }

    private double convert_y(LatLng latLng) {
        return (6371000 * cos(latLng.latitude) * sin(latLng.longitude));
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng weimar = new LatLng(50.980557, 11.332340);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Schloss Weimar"));
                Set<String> set = settings.getStringSet("set", new HashSet<String>());
                set.add(latLng.toString());
                SharedPreferences.Editor editor = settings.edit();
                editor.putStringSet("set", set);
                // Commit the edits!
                editor.commit();
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

        //mMap.addMarker(new MarkerOptions().position(weimar).title("Schloss Weimar"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(weimar, 18));


    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context

    }


}
