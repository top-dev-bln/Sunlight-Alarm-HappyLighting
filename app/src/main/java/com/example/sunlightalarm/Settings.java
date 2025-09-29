package com.example.sunlightalarm;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import android.widget.Button;
import android.widget.LinearLayout;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;



public class Settings extends AppCompatActivity {


    String macAddress;
    BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic targetChar;



    private static final int REQ_PERMISSIONS = 100;
    private BluetoothAdapter bluetoothAdapter;

    LinearLayout controls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


         bluetoothGatt = BleManager.getInstance().gatt;
         targetChar = BleManager.getInstance().targetChar;


        Button btnON = findViewById(R.id.button_ON);
        Button btnOFF = findViewById(R.id.button_OFF);
        Button btnDisconnect = findViewById(R.id.button_Disconnect);
        controls = findViewById(R.id.controls);


        btnON.setOnClickListener(v -> {
            if (targetChar != null) {
                targetChar.setValue(new byte[]{(byte) 204, (byte) 35, (byte) 51});
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(Settings.this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                    Log.e("BLE", "BLUETOOTH_CONNECT permission not granted, cannot discover services");
                    return;
                }
                bluetoothGatt.writeCharacteristic(targetChar);
            } else {
                Log.e("BLE", "Target characteristic not found");
            }
        });


        btnOFF.setOnClickListener(v -> {
            if (targetChar != null) {
                targetChar.setValue(new byte[]{(byte) 204, (byte) 36, (byte) 51});
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(Settings.this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                    Log.e("BLE", "BLUETOOTH_CONNECT permission not granted, cannot discover services");
                    return;
                }
                bluetoothGatt.writeCharacteristic(targetChar);
            } else {
                Log.e("BLE", "Target characteristic not found");
            }
        });


        btnDisconnect.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(Settings.this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                Log.e("BLE", "BLUETOOTH_CONNECT permission not granted, cannot discover services");
                return;
            }
            bluetoothGatt.disconnect();
            SharedPreferences prefs = getSharedPreferences("Selected", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("device", null);
            editor.apply();

            Intent intent = new Intent(Settings.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        macAddress = getIntent().getStringExtra("MAC_ADDRESS");
        Log.d("BLE", "MAC address: " + macAddress);


        checkAndRequestPermissions();

        ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d("BLE", "Bluetooth enabled");
                    } else {
                        Log.d("BLE", "Bluetooth not enabled");
                    }
                }
        );

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e("BLE", "BluetoothManager not available");
            finish();
            return;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e("BLE", "Bluetooth not supported");
            finish();
            return;
        }


        if (!bluetoothAdapter.isEnabled()) {
            enableBtLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }


    }



    public void checkAndRequestPermissions() {
        ArrayList<String> needed = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.BLUETOOTH_SCAN);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), REQ_PERMISSIONS);
        }
    }
}
