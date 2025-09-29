package com.example.sunlightalarm;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

public class BleManager {
    private static BleManager instance;
    public BluetoothGatt gatt;
    public BluetoothGattCharacteristic targetChar;

    private BleManager() {}

    public static BleManager getInstance() {
        if (instance == null) instance = new BleManager();
        return instance;
    }
}
