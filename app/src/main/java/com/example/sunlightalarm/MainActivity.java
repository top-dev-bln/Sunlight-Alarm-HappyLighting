package com.example.sunlightalarm;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
        private ArrayList<ScanResult> devices;
        private Context context;




        public CardAdapter(Context context, ArrayList<ScanResult> devices) {
            this.context = context;
            this.devices = devices;

        }





        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_device, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (position >= devices.size()) {
                return;
            }
            ScanResult result = devices.get(position);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                holder.nameTextView.setText("Unknown");
                return;
            }
            String name = result.getDevice().getName();
            String address = result.getDevice().getAddress();
            int rssi = result.getRssi();
            holder.adressTextView.setText(address);
            holder.rssiTextView.setText(String.valueOf(rssi));
            holder.nameTextView.setText(name != null ? name : "Unknown");
        }


        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView adressTextView;
            TextView rssiTextView;



            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.deviceName);
                adressTextView = itemView.findViewById(R.id.deviceAddress);
                rssiTextView = itemView.findViewById(R.id.deviceRssi);
                itemView.setOnClickListener(v -> {
                    LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                    View dialogView = inflater.inflate(R.layout.confirm, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    View cardView = dialogView.findViewById(R.id.dialogCard);

                    TextView dialogName = cardView.findViewById(R.id.deviceName);
                    TextView dialogAddress = cardView.findViewById(R.id.deviceAddress);
                    TextView dialogRssi = cardView.findViewById(R.id.deviceRssi);

                    dialogName.setText(nameTextView.getText().toString());
                    dialogAddress.setText(adressTextView.getText().toString());
                    dialogRssi.setText(rssiTextView.getText().toString());


                    dialogView.findViewById(R.id.buttonSelect);
                    dialogView.findViewById(R.id.buttonCancel);


                    dialogView.findViewById(R.id.buttonSelect).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences prefs = getSharedPreferences("Selected", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("device", devices.get(getBindingAdapterPosition()).getDevice().getAddress());
                            editor.apply();
                            dialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, Controller.class);
                            intent.putExtra("MAC_ADDRESS", devices.get(getBindingAdapterPosition()).getDevice().getAddress());
                            startActivity(intent);
                        }
                    });


                    dialogView.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });





                });
            }

        }
    }

    private static final int REQ_PERMISSIONS = 100;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;

    HashMap<String,ScanResult> foundDevices = new HashMap<>();

    private RecyclerView recyclerView;
    private CardAdapter adapter;










    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences def = getSharedPreferences("Protocols", MODE_PRIVATE);

        if (!def.contains("Default")) {
            try {
                JSONObject defaultProtocol = new JSONObject();
                defaultProtocol.put("name", "Default");
                defaultProtocol.put("duration", 5);

                JSONArray rgbArray = new JSONArray();

                // Minute 1
                JSONObject rgb1 = new JSONObject();
                rgb1.put("r", 250);
                rgb1.put("g", 0);
                rgb1.put("b", 0);
                rgbArray.put(rgb1);

                // Minute 2
                JSONObject rgb2 = new JSONObject();
                rgb2.put("r", 255);
                rgb2.put("g", 128);
                rgb2.put("b", 0);
                rgbArray.put(rgb2);

                // Minute 3
                JSONObject rgb3 = new JSONObject();
                rgb3.put("r", 255);
                rgb3.put("g", 255);
                rgb3.put("b", 0);
                rgbArray.put(rgb3);

                // Minute 4
                JSONObject rgb4 = new JSONObject();
                rgb4.put("r", 0);
                rgb4.put("g", 255);
                rgb4.put("b", 0);
                rgbArray.put(rgb4);

                // Minute 5
                JSONObject rgb5 = new JSONObject();
                rgb5.put("r", 0);
                rgb5.put("g", 0);
                rgb5.put("b", 255);
                rgbArray.put(rgb5);

                defaultProtocol.put("rgbValues", rgbArray);

                def.edit().putString("Default", defaultProtocol.toString()).apply();
                Log.d("Init", "Default protocol created manually with 5 RGB values");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        SharedPreferences prefs = getSharedPreferences("Selected", MODE_PRIVATE);
        String selectedDevice = prefs.getString("device", null);
        if (selectedDevice != null) {
            Log.d("BLE", "Selected device: " + selectedDevice);
            Intent intent = new Intent(MainActivity.this, Controller.class);
            intent.putExtra("MAC_ADDRESS", selectedDevice);
            startActivity(intent);

            Log.d("BLE", "Redirecting to second activity with device: " + selectedDevice);
        }
        else {
            Log.d("BLE", "No selected device");



        recyclerView = findViewById(R.id.RecycleCards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CardAdapter(this, new ArrayList<>(foundDevices.values()));

        recyclerView.setAdapter(adapter);


        checkAndRequestPermissions();


        ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        initScannerAndStartScan();
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

            initScannerAndStartScan();
        }
    }
    }
    private void initScannerAndStartScan() {

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


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (scanner != null) {
                scanner.stopScan(scanCallback);
                Log.d("BLE", "Scan finished");

            for (ScanResult result : foundDevices.values()) {
                Log.d("BLE", "Found device: " + result.getDevice().getName());
            }
            }
        }, 5000);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSIONS) {
            boolean allGranted = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    Log.e("BLE", "Permission denied: " + permissions[i]);
                }
            }

            if (allGranted) {
                Log.d("BLE", "All permissions granted");
                initScannerAndStartScan();
            } else {
                Log.e("BLE", "Cannot scan, some permissions denied");
            }
        }
    }





    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean hasBlePermissions() {

            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;


    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            BluetoothDevice BLEdevice = result.getDevice();
            String address = BLEdevice.getAddress();
            foundDevices.put(address, result);

            ArrayList<ScanResult> sortedList = new ArrayList<>(foundDevices.values());
            sortedList.sort((a, b) -> {
                String nameA = a.getDevice().getName();
                String nameB = b.getDevice().getName();

                boolean hasNameA = nameA != null && !nameA.isEmpty();
                boolean hasNameB = nameB != null && !nameB.isEmpty();

                if (hasNameA && !hasNameB) return -1;
                if (!hasNameA && hasNameB) return 1;
                return 0;
            });

            runOnUiThread(() -> {
                adapter = new CardAdapter(MainActivity.this, sortedList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "Scan failed: " + errorCode);
        }
    };
}
