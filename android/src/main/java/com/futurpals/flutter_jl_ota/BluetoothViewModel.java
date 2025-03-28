package com.futurpals.flutter_jl_ota;//package com.example.jl_ota;
//
//import android.bluetooth.BluetoothDevice;
//
//import androidx.lifecycle.ViewModel;
//
//import com.example.jl_ota.bluetooth.BluetoothHelper;
//import com.example.jl_ota.tool.ConfigHelper;
//
//public class BluetoothViewModel extends ViewModel {
//    protected final String tag = getClass().getSimpleName();
//    protected final ConfigHelper configHelper = ConfigHelper.getInstance();
//    protected final BluetoothHelper bluetoothHelper = BluetoothHelper.getInstance();
//
//    public boolean isConnected() {
//        return bluetoothHelper.isConnected();
//    }
//
//    public boolean isDeviceConnected(BluetoothDevice device) {
//        return bluetoothHelper.isDeviceConnected(device);
//    }
//
//    public BluetoothDevice getConnectedDevice() {
//        return bluetoothHelper.getConnectedDevice();
//    }
//}