package com.example.sunlightalarm;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;



public class Controller extends AppCompatActivity {


    String macAddress;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic targetChar;




    private static final int REQ_PERMISSIONS = 100;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;

    LinearLayout controls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Button btnON = findViewById(R.id.button_ON);
        Button btnOFF = findViewById(R.id.button_OFF);
        Button btnDisconnect = findViewById(R.id.button_Disconnect);
        controls = findViewById(R.id.controls);


        btnON.setOnClickListener(v -> {
            if (targetChar != null) {
                targetChar.setValue(new byte[]{(byte)204, (byte)35, (byte)51});
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(Controller.this, Manifest.permission.BLUETOOTH_CONNECT)
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
                targetChar.setValue(new byte[]{(byte)204, (byte)36, (byte)51});
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(Controller.this, Manifest.permission.BLUETOOTH_CONNECT)
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
                        ActivityCompat.checkSelfPermission(Controller.this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                    Log.e("BLE", "BLUETOOTH_CONNECT permission not granted, cannot discover services");
                    return;
                }
                bluetoothGatt.disconnect();
                SharedPreferences prefs = getSharedPreferences("Selected", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("device", null);
                editor.apply();

                Intent intent = new Intent(Controller.this,MainActivity.class);
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
                        initScannerandConnect();
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
        } else {

            initScannerandConnect();
        }


    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean hasBlePermissions() {

        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;


    }
    private void initScannerandConnect() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasBlePermissions()) {
                Log.d("BLE", "Permissions not granted yet, cannot start scan");
                return;
            }
        }


        scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) {
            Log.e("BLE", "BLE scanner not available");
            return;
        }

        Log.d("BLE", "Starting BLE scan");



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        scanner.startScan(scanCallback);


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
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(Controller.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            BluetoothDevice BLEdevice = result.getDevice();
            String address = BLEdevice.getAddress();

            Log.d("BLE", "Found device: " + address);

            if (address.equals(macAddress)) {
                Log.d("BLE", "Target device found: " + address);
                scanner.stopScan(this);
                ConnectToDevice(BLEdevice);
            }



        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Scan failed: " + errorCode);
        }
    };

    private void ConnectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to device");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            ActivityCompat.checkSelfPermission(Controller.this, Manifest.permission.BLUETOOTH_CONNECT)
                                    != PackageManager.PERMISSION_GRANTED) {
                        Log.e("BLE", "BLUETOOTH_CONNECT permission not granted, cannot discover services");
                        return;
                    }
                    Log.d("BLE", "Discovering services");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from device");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Services discovered");




                    for (BluetoothGattService service : gatt.getServices()) {
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            int props = characteristic.getProperties();
                            if ((props & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 ||
                                    (props & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                                targetChar = characteristic;
                                Log.d("BLE", "Target characteristic found: " + characteristic.getUuid());
                                runOnUiThread(() -> {
                                    controls.setVisibility(View.VISIBLE);
                                });
                                break;
                            }
                        }
                        if (targetChar != null) break;
                    }

                }
            }
        });
    }









}