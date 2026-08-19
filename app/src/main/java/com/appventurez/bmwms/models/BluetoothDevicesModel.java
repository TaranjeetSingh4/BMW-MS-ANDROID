package com.appventurez.bmwms.models;

import android.bluetooth.BluetoothDevice;

public class BluetoothDevicesModel {

    BluetoothDevice bluetoothDevice;
    int paired;

    public BluetoothDevicesModel(BluetoothDevice bluetoothDevice, int paired) {
        this.bluetoothDevice = bluetoothDevice;
        this.paired = paired;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getPaired() {
        return paired;
    }

    public void setPaired(int paired) {
        this.paired = paired;
    }
}
