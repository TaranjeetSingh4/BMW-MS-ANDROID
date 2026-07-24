package com.ostron.EcoGov.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;


import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;

import java.util.UUID;

public class MyBluetoothService {

    Context context;

    BluetoothService service;

    MyEventListener eventListener;

    public interface MyEventListener{

        void getDevices(BluetoothDevice device);
        void getData(String data);
        void connectionStatus(BluetoothStatus bluetoothStatus);

    }


    public MyBluetoothService(Context context,MyEventListener eventListener) {

        this.context = context;
        this.eventListener = eventListener;

    }

    public void configService(){

        BluetoothConfiguration config = new BluetoothConfiguration();
        config.context = context.getApplicationContext();
        config.bluetoothServiceClass = BluetoothClassicService .class;
        config.bufferSize = 1024;
        config.characterDelimiter = '\n';
        config.deviceName = "BluetoothDemo";
        config.callListenersInMainThread = true;

        config.uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Required

        BluetoothService.init(config);

        service = BluetoothService.getDefaultInstance();

    }

    public void startScanService(){

        service.setOnScanCallback(new BluetoothService.OnBluetoothScanCallback() {
            @Override
            public void onDeviceDiscovered(BluetoothDevice device, int rssi) {

                eventListener.getDevices(device);
//                if (device.getAddress().equals("00:18:E4:34:D0:AC") && device.getBondState() == BluetoothDevice.BOND_NONE){
//                    eventListener.getDevices(device);
//                }

            }

            @Override
            public void onStartScan() {

            }

            @Override
            public void onStopScan() {

            }
        });

        service.startScan();

    }

    public void stopBluetoothService(){
        service.stopService();
    }

//    public void stopScanService(){
//        service.stopScan();
//    }

    public void startDisconnectService(){
        service.disconnect();
    }

    public void connectDevice(BluetoothDevice bluetoothDevice){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice mDevice = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());

        service.setOnEventCallback(new BluetoothService.OnBluetoothEventCallback() {
            @Override
            public void onDataRead(byte[] buffer, int length) {
                eventListener.getData(new String(buffer));
            }

            @Override
            public void onStatusChange(BluetoothStatus status) {

                eventListener.connectionStatus(status);

            }

            @Override
            public void onDeviceName(String deviceName) {

            }

            @Override
            public void onToast(String message) {

            }

            @Override
            public void onDataWrite(byte[] buffer) {

            }
        });

        service.connect(mDevice);
    }

}
