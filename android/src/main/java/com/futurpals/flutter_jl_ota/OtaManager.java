package com.futurpals.flutter_jl_ota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.futurpals.flutter_jl_ota.ble.BleManager;
import com.futurpals.flutter_jl_ota.ble.interfaces.BleEventCallback;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.IActionCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;

import java.util.UUID;

class OtaManager extends BluetoothOTAManager {
    private static final String TAG = "OtaManager";
    private final Context context;
    private final BleManager bleManager;
    private String mac;
    private String deviceName;
    private static final long SCAN_TIMEOUT = 20 * 1000L;
    private FlutterJlOtaPlugin.OtaProgressCallback progressCallback;

    public OtaManager(Context context, String mac, String deviceName) {
        super(context);
        this.context = context;
        this.mac = mac;
        this.deviceName = deviceName;
        this.bleManager = new BleManager(context, mac, deviceName);
    }

    public void startScan() {
        bleManager.startLeScan(SCAN_TIMEOUT);
    }

    public void connectDevice(String uuid) {
        this.mac = uuid;
        BluetoothDevice device = getConnectedDeviceById(uuid);
        if (device != null) {
            bleManager.connectBleDevice(device);
        }
        registerBleCallbacks();
    }

    public void getDeviceInfo(IActionCallback<TargetInfoResponse> callback) {
        queryMandatoryUpdate(callback);
    }

    public void startOtaUpdate(String filePath, FlutterJlOtaPlugin.OtaProgressCallback callback) {
        this.progressCallback = callback;
        BluetoothOTAConfigure config = BluetoothOTAConfigure.createDefault()
                .setPriority(BluetoothOTAConfigure.PREFER_BLE)
                .setUseAuthDevice(true)
                .setBleIntervalMs(500)
                .setTimeoutMs(3000)
                .setMtu(500)
                .setNeedChangeMtu(false)
                .setUseReconnect(true)
                .setFirmwareFilePath(filePath);
        configure(config);
        startOTA(new IUpgradeCallback() {
            @Override
            public void onStartOTA() {
                if (progressCallback != null) progressCallback.onProgress(0, "STARTED");
            }

            @Override
            public void onNeedReconnect(String addr, boolean isNewReconnectWay) {
                if (getBluetoothOption().isUseReconnect()) {
                    reConnect(addr, isNewReconnectWay);
                }
            }

            @Override
            public void onProgress(int type, float progress) {
                int progressInt = (int) progress;
                if (progressCallback != null) {
                    progressCallback.onProgress(progressInt, type == 0 ? "DOWNLOADING_LOADER" : "UPGRADING_FIRMWARE");
                }
            }

            @Override
            public void onStopOTA() {
                if (progressCallback != null) progressCallback.onProgress(100, "COMPLETED");
                release();
            }

            @Override
            public void onCancelOTA() {
                if (progressCallback != null) progressCallback.onProgress(0, "CANCELED");
            }

            @Override
            public void onError(BaseError error) {
                if (progressCallback != null)
                    progressCallback.onProgress(0, "ERROR: " + error.getMessage());
            }
        });
    }

    public void cancelOtaUpdate(IActionCallback<Integer> callback) {
        // 双备份 OTA 才支持取消，这里假设直接停止
        cancelOTA();
        callback.onSuccess(0); // 假设 0 表示成功
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return bleManager.getConnectedBtDevice();
    }

    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {
        return bleManager.getConnectedBtGatt(getConnectedDevice());
    }

    @Override
    public void connectBluetoothDevice(BluetoothDevice device) {
        bleManager.connectBleDevice(device);
    }

    public void reConnect(String address, boolean isUseAdv) {
        bleManager.reconnectDevice(address, isUseAdv);
    }

    @Override
    public void disconnectBluetoothDevice(BluetoothDevice device) {
        bleManager.disconnectBleDevice(device);
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        bleManager.writeDataByBleAsync(device, BleManager.BLE_UUID_SERVICE, BleManager.BLE_UUID_WRITE, data, (device1, serviceUUID, characteristicUUID, result, data1) -> {
        });
        return true;
    }

    @Override
    public void release() {
        super.release();
        bleManager.destroy();
    }

    private void registerBleCallbacks() {
        bleManager.registerBleEventCallback(new BleEventCallback() {
            @Override
            public void onBleConnection(BluetoothDevice device, int status) {
                int connectStatus = changeConnectStatus(status);
                onBtDeviceConnection(device, connectStatus);
            }

            @Override
            public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
                onReceiveDeviceData(device, data);
            }

            @Override
            public void onBleDataBlockChanged(BluetoothDevice device, int block, int status) {
                onMtuChanged(getConnectedBluetoothGatt(), block, status);
            }
        });
    }

    private int changeConnectStatus(int status) {
        switch (status) {
            case BluetoothProfile.STATE_DISCONNECTED:
            case BluetoothProfile.STATE_DISCONNECTING:
                return StateCode.CONNECTION_DISCONNECT;
            case BluetoothProfile.STATE_CONNECTED:
                return StateCode.CONNECTION_OK;
            case BluetoothProfile.STATE_CONNECTING:
                return StateCode.CONNECTION_CONNECTING;
            default:
                return StateCode.CONNECTION_DISCONNECT;
        }
    }

    private BluetoothDevice getConnectedDeviceById(String uuid) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            return adapter.getRemoteDevice(uuid);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid UUID: " + uuid);
            return null;
        }
    }
}