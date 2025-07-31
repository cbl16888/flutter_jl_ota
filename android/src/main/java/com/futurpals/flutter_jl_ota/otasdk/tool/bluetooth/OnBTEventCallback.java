package com.futurpals.flutter_jl_ota.otasdk.tool.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.model.BleScanInfo;

import java.util.UUID;

/**
 * @author zqjasonZhong
 * @since 2022/9/14
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 蓝牙事件回调
 */
public class OnBTEventCallback {

    public void onAdapterChange(boolean bEnabled) {

    }

    public void onDiscoveryChange(boolean bStart, int scanType) {

    }

    public void onDiscovery(BluetoothDevice device, BleScanInfo bleScanMessage) {

    }

    public void onDeviceConnection(BluetoothDevice device, int way, int status) {

    }

    public void onReceiveData(BluetoothDevice device, int way, UUID uuid, byte[] data) {

    }

    public void onBleMtuChange(BluetoothDevice device, int mtu, int status) {

    }
}