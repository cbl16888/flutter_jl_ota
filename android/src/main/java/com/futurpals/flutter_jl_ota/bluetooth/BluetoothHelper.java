package com.futurpals.flutter_jl_ota.bluetooth;//package com.example.jl_ota.bluetooth;
//
//
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//
//import com.example.jl_ota.ble.BleManager;
//import com.example.jl_ota.ble.interfaces.BleEventCallback;
//import com.example.jl_ota.ble.model.BleScanInfo;
//import com.example.jl_ota.tool.AppUtil;
//import com.example.jl_ota.tool.ConfigHelper;
//import com.example.jl_ota.tool.OtaConstant;
//import com.jieli.jl_bt_ota.util.BluetoothUtil;
//import com.jieli.jl_bt_ota.util.CHexConver;
//import com.jieli.jl_bt_ota.util.JL_Log;
//
//import java.util.UUID;
//
//public class BluetoothHelper {
//
//    private ConfigHelper configHelper = ConfigHelper.getInstance();
//    private BleManager bleManager = BleManager.getInstance();
//
//    private BTEventCbHelper btEventCbHelper = new BTEventCbHelper();
//
//    private static final String TAG = "BluetoothHelper";
//
//    private static volatile BluetoothHelper instance;
//
//    public static BluetoothHelper getInstance() {
//        if (instance == null) {
//            synchronized (BluetoothHelper.class) {
//                if (instance == null) {
//                    instance = new BluetoothHelper();
//                }
//            }
//        }
//        return instance;
//    }
//
//    private final BleEventCallback bleEventCallback = new BleEventCallback() {
//        @Override
//        public void onAdapterChange(boolean bEnabled) {
//            btEventCbHelper.onAdapterChange(bEnabled);
//        }
//
//        @Override
//        public void onDiscoveryBleChange(boolean bStart) {
//            btEventCbHelper.onDiscoveryChange(bStart, OtaConstant.PROTOCOL_BLE);
//        }
//
//        @Override
//        public void onDiscoveryBle(BluetoothDevice device, BleScanInfo bleScanMessage) {
//            btEventCbHelper.onDiscovery(device, bleScanMessage);
//        }
//
//        @Override
//        public void onBleConnection(BluetoothDevice device, int status) {
//            btEventCbHelper.onDeviceConnection(device, OtaConstant.PROTOCOL_BLE, status);
//        }
//
//        @Override
//        public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
//            btEventCbHelper.onReceiveData(device, OtaConstant.PROTOCOL_BLE, characteristicsUuid, data);
//        }
//
//        @Override
//        public void onBleDataBlockChanged(BluetoothDevice device, int block, int status) {
//            btEventCbHelper.onBleMtuChange(device, block, status);
//        }
//    };
//
//
//
//    private BluetoothHelper() {
//        if (configHelper.isBleWay()) {
//            bleManager.registerBleEventCallback(bleEventCallback);
//        }
//    }
//
//    public void destroy() {
//        bleManager.unregisterBleEventCallback(bleEventCallback);
//        bleManager.destroy();
//
//        btEventCbHelper.release();
//        instance = null;
//    }
//
//    public void registerCallback(OnBTEventCallback callback) {
//        btEventCbHelper.registerCallback(callback);
//    }
//
//    public void unregisterCallback(OnBTEventCallback callback) {
//        btEventCbHelper.unregisterCallback(callback);
//    }
//
//    public boolean isConnected() {
//        return getConnectedDevice() != null;
//    }
//
//    public boolean isDeviceConnected(BluetoothDevice device) {
//        return BluetoothUtil.deviceEquals(getConnectedDevice(), device);
//    }
//
//
//
//    public boolean isConnecting() {
//        return configHelper.isBleWay() ? bleManager.isBleScanning() : null;
//    }
//
//    public BluetoothDevice getConnectedDevice() {
//        return configHelper.isBleWay() ? bleManager.getConnectedBtDevice() : null;
//    }
//
//    public BluetoothGatt getConnectedGatt() {
//        return configHelper.isBleWay() ? bleManager.getConnectedBtGatt(getConnectedDevice()) : null;
//    }
//
//    public int getBleMtu() {
//        return configHelper.isBleWay() ? bleManager.getBleMtu(getConnectedDevice()) : 20;
//    }
//
//
//
//
//
//    public boolean connectDevice(BluetoothDevice device) {
//        return configHelper.isBleWay() ? bleManager.connectBleDevice(device) : null;
//    }
//
//    public void disconnectDevice(BluetoothDevice device) {
//        if (configHelper.isBleWay()) {
////            bleManager.disconnectBleDevice(device);
//        } else {
//
//        }
//    }
//
//    public boolean connectBleDevice(BluetoothDevice device) {
//        return bleManager.connectBleDevice(device);
//    }
//
//    public void reconnectDevice(String address,boolean isUseNewAdv){
//        bleManager.reconnectDevice(address, isUseNewAdv);
//
//    }
//
//
//    public boolean writeDataToDevice(BluetoothDevice bluetoothDevice, byte[] byteArray) {
//        if (bluetoothDevice == null || byteArray == null || byteArray.length == 0) {
//            return false;
//        }
//        if (bleManager.getConnectedBtDevice() != null) { // Currently connected device is BLE
//            bleManager.writeDataByBleAsync(
//                    bluetoothDevice,
//                    BleManager.BLE_UUID_SERVICE,
//                    BleManager.BLE_UUID_WRITE,
//                    byteArray,
//                    (device, serviceUUID, characteristicUUID, result, data) -> {
//                        JL_Log.i(TAG, "-writeDataByBleAsync- device:" + printDeviceInfo(device) +
//                                ", result = " + result + ",\ndata: [" + CHexConver.byte2HexStr(data) + "]");
//                    }
//            );
//        }
//        return true;
//    }
//
//    private String printDeviceInfo(BluetoothDevice device) {
//        return AppUtil.printBtDeviceInfo(device);
//    }
//}
