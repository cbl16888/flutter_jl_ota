package com.futurpals.flutter_jl_ota.otasdk.tool.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.interfaces.OnWriteDataCallback;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.spp.interfaces.OnWriteSppDataCallback;
import com.jieli.jl_bt_ota.util.BluetoothUtil;
import com.jieli.jl_bt_ota.util.CHexConver;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.futurpals.flutter_jl_ota.otasdk.tool.config.ConfigHelper;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.BleManager;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.interfaces.BleEventCallback;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.model.BleScanInfo;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.spp.SppManager;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.spp.interfaces.SppEventCallback;
import com.futurpals.flutter_jl_ota.otasdk.util.AppUtil;
import com.futurpals.flutter_jl_ota.otasdk.data.constant.OtaConstant;

import java.util.UUID;

/**
 * @author zqjasonZhong
 * @since 2022/9/14
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 蓝牙操作辅助类
 */
public class BluetoothHelper {

    private static final String TAG = "BluetoothHelper";

    private static volatile BluetoothHelper instance;

    private final ConfigHelper configHelper = ConfigHelper.getInstance();
    private final BleManager bleManager = BleManager.getInstance();
    private final SppManager sppManager = SppManager.getInstance();
    private final BTEventCbHelper btEventCbHelper = new BTEventCbHelper();

    private final BleEventCallback bleEventCallback = new BleEventCallback() {

        @Override
        public void onAdapterChange(boolean bEnabled) {
            btEventCbHelper.onAdapterChange(bEnabled);
        }

        @Override
        public void onDiscoveryBleChange(boolean bStart) {
            btEventCbHelper.onDiscoveryChange(bStart, OtaConstant.PROTOCOL_BLE);
        }

        @Override
        public void onDiscoveryBle(BluetoothDevice device, BleScanInfo bleScanMessage) {
            btEventCbHelper.onDiscovery(device, bleScanMessage);
        }

        @Override
        public void onBleConnection(BluetoothDevice device, int status) {
            btEventCbHelper.onDeviceConnection(device, OtaConstant.PROTOCOL_BLE, status);
        }

        @Override
        public void onBleDataNotification(
                BluetoothDevice device,
                UUID serviceUuid,
                UUID characteristicsUuid,
                byte[] data
        ) {
            btEventCbHelper.onReceiveData(
                    device,
                    OtaConstant.PROTOCOL_BLE,
                    characteristicsUuid,
                    data
            );
        }

        @Override
        public void onBleDataBlockChanged(BluetoothDevice device, int block, int status) {
            btEventCbHelper.onBleMtuChange(device, block, status);
        }
    };

    private final SppEventCallback sppEventCallback = new SppEventCallback() {

        @Override
        public void onAdapterChange(boolean bEnabled) {
            btEventCbHelper.onAdapterChange(bEnabled);
        }

        @Override
        public void onDiscoveryDeviceChange(boolean bStart) {
            btEventCbHelper.onDiscoveryChange(bStart, OtaConstant.PROTOCOL_SPP);
        }

        @Override
        public void onDiscoveryDevice(BluetoothDevice device, int rssi) {
            btEventCbHelper.onDiscovery(device, new BleScanInfo().setRssi(rssi));
        }

        @Override
        public void onSppConnection(BluetoothDevice device, UUID uuid, int status) {
            if (status == BluetoothProfile.STATE_CONNECTED && configHelper.isUseMultiSppChannel()
                    && !UUID.fromString(configHelper.getCustomSppChannel()).equals(uuid)
            ) {
                JL_Log.i(TAG, "onSppConnection", "skip custom uuid = " + uuid);
                return;
            }
            JL_Log.d(
                    TAG, "onSppConnection",
                    "device : " + printDeviceInfo(device) + ", uuid = " + uuid + ", status = " + status
            );
            btEventCbHelper.onDeviceConnection(device, OtaConstant.PROTOCOL_SPP, status);
        }

        @Override
        public void onReceiveSppData(BluetoothDevice device, UUID uuid, byte[] data) {
            btEventCbHelper.onReceiveData(device, OtaConstant.PROTOCOL_SPP, uuid, data);
        }
    };

    private BluetoothHelper() {
//        if (configHelper.isBleWay()) {
        bleManager.registerBleEventCallback(bleEventCallback);
//        } else {
        sppManager.registerSppEventCallback(sppEventCallback);
//        }
    }

    public static BluetoothHelper getInstance() {
        if (instance == null) {
            synchronized (BluetoothHelper.class) {
                if (instance == null) {
                    instance = new BluetoothHelper();
                }
            }
        }
        return instance;
    }

    public void destroy() {
        bleManager.unregisterBleEventCallback(bleEventCallback);
        bleManager.destroy();
        sppManager.unregisterSppEventCallback(sppEventCallback);
        sppManager.release();
        btEventCbHelper.release();
        instance = null;
    }

    public void registerCallback(OnBTEventCallback callback) {
        btEventCbHelper.registerCallback(callback);
    }

    public void unregisterCallback(OnBTEventCallback callback) {
        btEventCbHelper.unregisterCallback(callback);
    }

    public boolean isConnected() {
        return getConnectedDevice() != null;
    }

    public boolean isDeviceConnected(BluetoothDevice device) {
        return BluetoothUtil.deviceEquals(getConnectedDevice(), device);
    }

    public boolean isScanning() {
        if (configHelper.isBleWay()) {
            return bleManager.isBleScanning();
        } else {
            return sppManager.isScanning();
        }
    }

    public boolean isConnecting() {
        if (configHelper.isBleWay()) {
            return bleManager.isBleScanning();
        } else {
            return sppManager.isSppConnecting();
        }
    }

    public BluetoothDevice getConnectedDevice() {
        if (configHelper.isBleWay()) {
            return bleManager.getConnectedBtDevice();
        } else {
            return sppManager.getConnectedSppDevice();
        }
    }

    public BluetoothDevice getConnectingDevice() {
        if (configHelper.isBleWay()) {
            return bleManager.getConnectingDevice();
        } else {
            return sppManager.getConnectingSppDevice();
        }
    }

    public BluetoothGatt getConnectedGatt() {
        if (configHelper.isBleWay()) {
            return bleManager.getConnectedBtGatt(getConnectedDevice());
        } else {
            return null;
        }
    }

    public int getBleMtu() {
        if (configHelper.isBleWay()) {
            return bleManager.getBleMtu(getConnectedDevice());
        } else {
            return 20;
        }
    }

    public boolean startScan(long timeout) {
        if (configHelper.isBleWay()) {
            return bleManager.startLeScan(timeout);
        } else {
            return sppManager.startDeviceScan(timeout);
        }
    }

    public void stopScan() {
        if (configHelper.isBleWay()) {
            bleManager.stopLeScan();
        } else {
            sppManager.stopDeviceScan();
        }
    }

    public boolean connectDevice(BluetoothDevice device) {
        if (configHelper.isBleWay()) {
            return bleManager.connectBleDevice(device);
        } else {
            return sppManager.connectSpp(device);
        }
    }

    public void disconnectDevice(BluetoothDevice device) {
        if (configHelper.isBleWay()) {
            bleManager.disconnectBleDevice(device);
        } else {
            sppManager.disconnectSpp(device, null);
        }
    }

    public boolean connectBleDevice(BluetoothDevice device) {
        return bleManager.connectBleDevice(device);
    }

    public void reconnectDevice(String address, boolean isUseNewAdv) {
        if (configHelper.isBleWay()) {
            bleManager.reconnectDevice(address, isUseNewAdv);
        } else {
            //TODO:需要增加SPP自定义回连方式
        }
    }

    public boolean writeDataToDevice(BluetoothDevice bluetoothDevice, byte[] byteArray) {
        if (null == bluetoothDevice || null == byteArray || byteArray.length == 0) return false;
        if (bleManager.getConnectedBtDevice() != null) {//目前连接的设备是ble
            bleManager.writeDataByBleAsync(
                    bluetoothDevice,
                    BleManager.BLE_UUID_SERVICE,
                    BleManager.BLE_UUID_WRITE,
                    byteArray,
                    new OnWriteDataCallback() {
                        @Override
                        public void onBleResult(BluetoothDevice device, UUID serviceUUID, UUID characteristicUUID, boolean result, byte[] data) {
                            JL_Log.d(
                                    TAG, "writeDataByBleAsync",
                                    "device : " + printDeviceInfo(device) + ", result : " + result + ",\n" +
                                            "data : [" + CHexConver.byte2HexStr(data) + "]"
                            );
                        }
                    }
            );
        } else if (sppManager.getConnectedSppDevice() != null) {//目前连接的设备是Spp
            sppManager.writeDataToSppAsync(
                    bluetoothDevice,
                    SppManager.UUID_SPP,
                    byteArray,
                    new OnWriteSppDataCallback() {
                        @Override
                        public void onSppResult(BluetoothDevice device, UUID sppUUID, boolean result, byte[] data) {
                            JL_Log.d(
                                    TAG,
                                    "writeDataToSppAsync",
                                    "device : " + printDeviceInfo(device) + ", uuid : " + sppUUID + ", result : " + result + "," +
                                            "\ndata : " + CHexConver.byte2HexStr(data)
                            );
                        }
                    }
            );
        }
        return true;
    }

    private String printDeviceInfo(BluetoothDevice device) {
        return AppUtil.printBtDeviceInfo(device);
    }

}