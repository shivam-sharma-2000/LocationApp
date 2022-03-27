package com.example.locationapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.locationapp.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.security.auth.login.LoginException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    LocationManager locationManager;
    LocationListener locationListener;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng latLng;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Intent intent = getIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        if (MainActivity.firstTime) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                LatLng latLng1 = new LatLng(location.getLatitude(), location.getLongitude());
                                String currentPlace = getAddress(latLng1);
                                mMap.addMarker(new MarkerOptions().position(latLng1).title(currentPlace));
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 15));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,18),6000, null);
                            }
                        }
                    });
        }

        if (intent.getIntExtra("tag", 1000) != 1000 && intent.getIntExtra("tag", 1000) != -1) {
            mMap.clear();
            latLng = MainActivity.Locations.get(intent.getIntExtra("tag", 1000));
            String visitedPlace = getAddress(latLng);
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latLng).title(visitedPlace));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if (intent.getIntExtra("tag", 0) == -1) {
                    LatLng userLocation = new LatLng(latLng.latitude, latLng.longitude);
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    String address = getAddress(userLocation);
                    MainActivity.address.add(address);
                    MainActivity.Locations.add(userLocation);
                    MainActivity.listView.setAdapter(MainActivity.arrayAdapter);
                    sharedPreferences = getApplicationContext().getSharedPreferences("com.example.locationapp", Context.MODE_PRIVATE);
                    ArrayList<String> latitude = new ArrayList<>();
                    ArrayList<String> longitude = new ArrayList<>();
                    try {
                        for (LatLng coords : MainActivity.Locations) {
                            latitude.add(Double.toString(coords.latitude));
                            longitude.add(Double.toString(coords.longitude));
                        }
                        sharedPreferences.edit().putString("address", ObjectSerializer.serialize(MainActivity.address)).apply();
                        sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitude)).apply();
                        sharedPreferences.edit().putString("longs", ObjectSerializer.serialize(longitude)).apply();
                    } catch (Exception e) {
                        Log.i("error", e.toString());
                    }
                    Toast.makeText(MapsActivity.this, "Location is saved;)", Toast.LENGTH_SHORT).show();
//                    locationsStorage.edit().putStringSet("Locations", MainActivity.Locations).apply();
                }
            }
        });

    }

    public String getAddress(LatLng userLocation) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try {
            List<Address> listAddress = geocoder.getFromLocation(userLocation.latitude, userLocation.longitude, 1);
            if (listAddress != null && listAddress.size() > 0) {
                if (listAddress.get(0).getAddressLine(0) != null) {
                    address = listAddress.get(0).getAddressLine(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }
}