package com.example.sunlightalarm;

import android.content.Intent;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



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



    }
}