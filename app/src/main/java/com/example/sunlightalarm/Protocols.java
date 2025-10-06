package com.example.sunlightalarm;

import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Protocols extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocols);
        Button btnAdd = findViewById(R.id.buttonAdd);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Protocols.this,Protocol_create.class);
            startActivity(intent);
        });

        RecyclerView recycler = findViewById(R.id.RecycleCards);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("Protocols", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        List<Protocol> protocols = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String name = entry.getKey();
            String jsonString = (String) entry.getValue();

            try {
                JSONObject obj = new JSONObject(jsonString);
                int duration = obj.getInt("duration");
                JSONArray rgbArray = obj.getJSONArray("rgbValues");
                protocols.add(new Protocol(entry.getKey(), duration));
                Log.d("RGB", name + " | duration: " + duration + " | RGB count: " + rgbArray.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        View.OnClickListener listener = v -> {
            Protocol p = (Protocol) v.getTag();

            SharedPreferences q = getSharedPreferences("AppState", MODE_PRIVATE);
            q.edit()
                    .putString("LastUsedProtocol", p.name)
                    .putInt("LastUsedDuration", p.durationMinutes)
                    .apply();


            Intent intent = new Intent(this, Controller.class);
            intent.putExtra("PROTOCOL_NAME", p.name);

            Log.d("RGB", "Selected protocol: " + p.name);
            startActivity(intent);
        };


        ProtocolAdapter adapter = new ProtocolAdapter(this, protocols, listener);

        recycler.setAdapter(adapter);



    }
}