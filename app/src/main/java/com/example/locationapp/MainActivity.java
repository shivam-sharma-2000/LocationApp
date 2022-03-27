package com.example.locationapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static ArrayList<String> address = new ArrayList<String>();
    static ArrayAdapter<String> arrayAdapter;
    static ArrayList<LatLng> Locations = new ArrayList<LatLng>();
    static ListView listView;
    static boolean firstTime = true;
    String string = "";
    Intent intent;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = this.getSharedPreferences("com.example.locationapp", Context.MODE_PRIVATE);
        ArrayList<String> latitude = new ArrayList<>();
        ArrayList<String> longitude = new ArrayList<>();
        try {
            address = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("address", ObjectSerializer.serialize(new ArrayList<String>())));
            latitude = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats", ObjectSerializer.serialize(new ArrayList<String>())));
            longitude = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longs", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (Exception e) {
            Log.i("errorM", e.toString());
        }

        for (int i = 0; i < latitude.size(); i++) {
            Log.i("check", "yes");
            Locations.add(new LatLng(Double.parseDouble(latitude.get(i)), Double.parseDouble(longitude.get(i))));
        }

        intent = new Intent(getApplicationContext(), MapsActivity.class);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, address);
        listView = findViewById(R.id.addressList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                firstTime = false;
                intent.putExtra("tag", i);
                startActivity(intent);
            }
        });
    }

    /***
     *
     * Changing activity with method changeActivity
     * finish activity with method changeBackActivity
     *
     */
    public void changeActivity(View view) {
        intent.putExtra("tag", -1);
        firstTime = true;
        startActivity(intent);
    }

    public void changeBackActivity(View view) {
        finish();
    }

}