package com.example.healthmate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private MaterialButton btnTrack;
    private FloatingActionButton fabReset;

    // Keep track of user‐selected points
    private final List<LatLng> selectedPoints = new ArrayList<>();
    private boolean tracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        SupportMapFragment mapFrag = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);


        btnTrack = findViewById(R.id.btnTrack);
        fabReset = findViewById(R.id.fabReset);


        btnTrack.setOnClickListener(v -> {
            if (!tracking) startLocationUpdates();
            else stopLocationUpdates();
            tracking = !tracking;
            btnTrack.setText(tracking ? "Stop Tracking" : "Start Tracking");
        });


        fabReset.setOnClickListener(v -> resetMap());


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (mMap == null) return;

                for (var loc : result.getLocations()) {
                    LatLng p = new LatLng(loc.getLatitude(), loc.getLongitude());

                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMapClickListener(latLng -> {
            if (selectedPoints.size() >= 2) {
                resetMap();
            }
            selectedPoints.add(latLng);
            String title = selectedPoints.size() == 1 ? "Start" : "End";
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(title));

            if (selectedPoints.size() == 2) {
                drawRoute(selectedPoints.get(0), selectedPoints.get(1));
            }
        });
    }

    private void drawRoute(LatLng a, LatLng b) {
        mMap.addPolyline(new PolylineOptions()
                .add(a, b)
                .width(6)
                .color(Color.BLUE));
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(a).include(b).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private void resetMap() {
        selectedPoints.clear();
        mMap.clear();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        LocationRequest req = LocationRequest.create()
                .setInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(req,
                locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this,
                    "Location permission is needed to track.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
