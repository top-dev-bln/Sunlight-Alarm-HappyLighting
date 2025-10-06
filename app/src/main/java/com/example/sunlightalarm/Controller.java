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
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
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
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class Controller extends AppCompatActivity {


    public interface OnTimeSelectedListener {
        void onTimeSelected(int hour, int minute);
    }




    String macAddress;
    String ProtocolName;
    List<RGBAdapter.RGBValue> rgbValues;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic targetChar;




    private static final int REQ_PERMISSIONS = 100;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        ProtocolName = getIntent().getStringExtra("PROTOCOL_NAME");
        if (ProtocolName == null) {

            SharedPreferences q = getSharedPreferences("AppState", MODE_PRIVATE);
            String lastUsed = q.getString("LastUsedProtocol", null);
            int lastDuration = q.getInt("LastUsedDuration", 0);


            if (lastUsed != null) {
                Log.d("RGB", "Last used protocol: " + lastUsed);

                //TODO aici o sa se caute un nou protocol cu aceasi durata
                ProtocolName = lastUsed;

                SharedPreferences prefs = getSharedPreferences("Protocols", MODE_PRIVATE);


                if (prefs.contains(lastUsed)) {
                    ProtocolName = lastUsed;
                } else {
                    Log.d("RGB", "Last used protocol not found, using default");
                    ProtocolName = "Default";
                }

            } else {
                Log.d("RGB", "No last used protocol, using default");
                ProtocolName = "Default";
            }


        }



        SharedPreferences prefs = getSharedPreferences("Protocols", MODE_PRIVATE);
        //TODO in loc de null la default value sa avem un protocol default si sa nu mai fie stocat acolo
        String json = prefs.getString(ProtocolName, null);

        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                int duration = obj.getInt("duration");

                JSONArray rgbArray = obj.getJSONArray("rgbValues");
                rgbValues = new ArrayList<>();

                for (int i = 0; i < rgbArray.length(); i++) {
                    JSONObject rgb = rgbArray.getJSONObject(i);
                    int r = rgb.getInt("r");
                    int g = rgb.getInt("g");
                    int b = rgb.getInt("b");
                    rgbValues.add(new RGBAdapter.RGBValue(r, g, b));
                }

                // You now have the protocol details
                Log.d("RGB", "Loaded " + obj.getString("name"));
                Log.d("RGB", "Duration: " + duration);
                Log.d("RGB", "RGB entries: " + rgbValues.size());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("RGB", "Protocol not found");
        }





        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_protocols) {
                startActivity(new Intent(this, Protocols.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, Settings.class));
                return true;
            }

            return false;
        });

        LinearLayout protocolSelect = findViewById(R.id.protocol_select);
        LinearLayout freqSelect = findViewById(R.id.freq_select);
        LinearLayout startSelect = findViewById(R.id.start_select);
        LinearLayout endSelect = findViewById(R.id.end_select);

        protocolSelect.setOnClickListener(v -> {
            Toast.makeText(this, "Protocol select clicked", Toast.LENGTH_SHORT).show();
        });

        freqSelect.setOnClickListener(v -> {
            Toast.makeText(this, "Freq select clicked", Toast.LENGTH_SHORT).show();
        });

        startSelect.setOnClickListener(v ->
                showWheelTimePicker("Select Sunrise Time", (hour, minute) -> {
                    Toast.makeText(this, "Sunrise: " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
                })
        );

        endSelect.setOnClickListener(v ->
                showWheelTimePicker("Select Wake Up Time", (hour, minute) -> {
                    Toast.makeText(this, "Wake Up: " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
                })
        );


        Handler handler = new Handler(Looper.getMainLooper());
        int[] i = {0};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (i[0] < rgbValues.size()) {
                    RGBAdapter.RGBValue rgb = rgbValues.get(i[0]);
                    Log.d("RGB", "Minute" + i[0] + ": " + "R:" + rgb.r + " G:" + rgb.g + " B:" + rgb.b);
                    i[0]++;
                    handler.postDelayed(this, 1000); // run again after 1 second
                }
            }
        };

        Button btnPreview = findViewById(R.id.button_Preview);

        btnPreview.setOnClickListener(v -> {
               Log.d("RGB", "Preview button clicked");

            handler.post(runnable);


        });




        macAddress = getIntent().getStringExtra("MAC_ADDRESS");
        if (macAddress != null) {
            Log.d("BLE", "MAC address: " + macAddress);
        }



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


    private void applyBlurBehindDialog(View rootView, boolean enable) {
        if (enable) {
            RenderEffect blurEffect = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurEffect = RenderEffect.createBlurEffect(50f, 50f, Shader.TileMode.CLAMP);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rootView.setRenderEffect(blurEffect);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                rootView.setRenderEffect(null);
            }
        }
    }



    private void showWheelTimePicker(String title, OnTimeSelectedListener listener) {
        View view = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

        TextView pickerTitle = view.findViewById(R.id.picker_title);
        pickerTitle.setText(title);

        List<String> hours = new ArrayList<>();
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 24; i++) hours.add(String.format("%02d", i));
        for (int i = 0; i < 60; i++) minutes.add(String.format("%02d", i));

        List<String> circularHours = new ArrayList<>();
        List<String> circularMinutes = new ArrayList<>();
        int repeat = 100;
        for (int i = 0; i < repeat; i++) {
            circularHours.addAll(hours);
            circularMinutes.addAll(minutes);
        }

        RecyclerView hourRecycler = view.findViewById(R.id.hour_picker_recycler);
        RecyclerView minuteRecycler = view.findViewById(R.id.minute_picker_recycler);

        WheelAdapter hourAdapter = new WheelAdapter(this, circularHours);
        WheelAdapter minuteAdapter = new WheelAdapter(this, circularMinutes);

        LinearLayoutManager hourLayout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        LinearLayoutManager minuteLayout = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        hourRecycler.setLayoutManager(hourLayout);
        hourRecycler.setAdapter(hourAdapter);
        hourRecycler.setHasFixedSize(true);
        hourRecycler.setNestedScrollingEnabled(false);

        minuteRecycler.setLayoutManager(minuteLayout);
        minuteRecycler.setAdapter(minuteAdapter);
        minuteRecycler.setHasFixedSize(true);
        minuteRecycler.setNestedScrollingEnabled(false);

        LinearSnapHelper hourSnap = new LinearSnapHelper();
        hourSnap.attachToRecyclerView(hourRecycler);

        LinearSnapHelper minuteSnap = new LinearSnapHelper();
        minuteSnap.attachToRecyclerView(minuteRecycler);

        int middleHour = circularHours.size() / 2;
        int middleMinute = circularMinutes.size() / 2;
        hourRecycler.scrollToPosition(middleHour);
        minuteRecycler.scrollToPosition(middleMinute);

        RecyclerView.OnScrollListener hourScaleListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                int recyclerCenterY = recyclerView.getHeight() / 2;
                WheelAdapter adapter = (WheelAdapter) recyclerView.getAdapter();
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float childCenterY = (child.getTop() + child.getBottom()) / 2f;
                    float distance = Math.abs(recyclerCenterY - childCenterY);
                    float scale = 1f - Math.min(distance / recyclerCenterY, 1f);
                    if (recyclerView.getChildViewHolder(child) instanceof WheelAdapter.WheelViewHolder) {
                        WheelAdapter.WheelViewHolder holder = (WheelAdapter.WheelViewHolder) recyclerView.getChildViewHolder(child);
                        adapter.setItemScale(holder, scale);
                        if (scale >= 0.85f) {
                            holder.textView.setTextColor(Color.parseColor("#2196F3"));
                        } else {
                            holder.textView.setTextColor(Color.argb(255, 255, 255, 255));
                        }
                    }
                }
            }
        };

        RecyclerView.OnScrollListener minuteScaleListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                int recyclerCenterY = recyclerView.getHeight() / 2;
                WheelAdapter adapter = (WheelAdapter) recyclerView.getAdapter();
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    float childCenterY = (child.getTop() + child.getBottom()) / 2f;
                    float distance = Math.abs(recyclerCenterY - childCenterY);
                    float scale = 1f - Math.min(distance / recyclerCenterY, 1f);
                    if (recyclerView.getChildViewHolder(child) instanceof WheelAdapter.WheelViewHolder) {
                        WheelAdapter.WheelViewHolder holder = (WheelAdapter.WheelViewHolder) recyclerView.getChildViewHolder(child);
                        adapter.setItemScale(holder, scale);
                        if (scale >= 0.85f) {
                            holder.textView.setTextColor(Color.parseColor("#2196F3"));
                        } else {
                            holder.textView.setTextColor(Color.argb(255, 255, 255, 255));
                        }
                    }
                }
            }
        };

        hourRecycler.addOnScrollListener(hourScaleListener);
        minuteRecycler.addOnScrollListener(minuteScaleListener);

        BottomSheetDialog dialog = getBottomSheetDialog(view);

        Button confirm = view.findViewById(R.id.btn_ok);
        Button cancel = view.findViewById(R.id.btn_cancel);

        cancel.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            int selectedHour = 0;
            int selectedMinute = 0;

            View centerHour = hourSnap.findSnapView(hourLayout);
            if (centerHour != null) {
                selectedHour = hourLayout.getPosition(centerHour) % 24;
            }

            View centerMinute = minuteSnap.findSnapView(minuteLayout);
            if (centerMinute != null) {
                selectedMinute = minuteLayout.getPosition(centerMinute) % 60;
            }

            listener.onTimeSelected(selectedHour, selectedMinute);
            dialog.dismiss();
        });


        dialog.show();
    }

    @NonNull
    private BottomSheetDialog getBottomSheetDialog(View view) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        dialog.setContentView(view);

        dialog.setOnShowListener(d -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight((int) (screenHeight * 0.66f), true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(true);
            } else {
                dialog.getWindow().setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.66f)
                );
            }

            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                applyBlurBehindDialog(rootView, true);
            }
        });

        dialog.setCancelable(false);



        dialog.setOnDismissListener(d -> {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                applyBlurBehindDialog(rootView, false);
            }
        });
        return dialog;
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
                                BleManager.getInstance().targetChar = targetChar;
                                Log.d("BLE", "Target characteristic found: " + characteristic.getUuid());

                                break;
                            }
                        }
                        if (targetChar != null) break;
                    }

                }
            }
        });

        BleManager.getInstance().gatt = bluetoothGatt;
    }









}