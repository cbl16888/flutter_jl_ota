package com.futurpals.flutter_jl_ota.otasdk.tool.ota;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.util.Log;

import com.jieli.jl_bt_ota.constant.BluetoothConstant;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.impl.RcspAuth;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.util.BluetoothUtil;
import com.jieli.jl_bt_ota.util.CHexConver;
import com.jieli.jl_bt_ota.util.JL_Log;

import com.futurpals.flutter_jl_ota.otasdk.tool.bluetooth.BluetoothHelper;
import com.futurpals.flutter_jl_ota.otasdk.tool.bluetooth.OnBTEventCallback;
import com.futurpals.flutter_jl_ota.otasdk.tool.config.ConfigHelper;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.spp.SppManager;
import com.futurpals.flutter_jl_ota.otasdk.util.AppUtil;
import com.futurpals.flutter_jl_ota.otasdk.data.constant.OtaConstant;

import java.util.UUID;

/**
 * 用于RCSP的第三方SDK接入OTA流程
 */
public class OTAManager extends BluetoothOTAManager {
    private static final String TAG = "OTAManager";

    private final ConfigHelper configHelper = ConfigHelper.getInstance();
    private final BluetoothHelper bluetoothHelper = BluetoothHelper.getInstance();

    private final OnBTEventCallback btEventCallback = new OnBTEventCallback() {
        @Override
        public void onDeviceConnection(BluetoothDevice device, int way, int status) {
            super.onDeviceConnection(device, way, status);
            int connectionState = AppUtil.changeConnectStatus(status);
            JL_Log.i(TAG, "onDeviceConnection", "device : " + printDeviceInfo(device) + ", way = " + way +
                    ", status ：" + status + ", change status : " + connectionState);
            onBtDeviceConnection(device, connectionState);
        }

        @Override
        public void onReceiveData(BluetoothDevice device, int way, UUID uuid, byte[] data) {
            super.onReceiveData(device, way, uuid, data);
            JL_Log.d(TAG, "onReceiveData",
                    "device : " + printDeviceInfo(device) + ", way = " + way +
                            "\nuuid = " + uuid + ", data ：" + CHexConver.byte2HexStr(data));
            
            if (way == OtaConstant.PROTOCOL_SPP && !SppManager.UUID_SPP.equals(uuid)) {
                JL_Log.d(TAG, "onReceiveData", "skip spec");
                return;
            }
            onReceiveDeviceData(device, data);
        }

        @Override
        public void onBleMtuChange(BluetoothDevice device, int mtu, int status) {
            super.onBleMtuChange(device, mtu, status);
            onMtuChanged(bluetoothHelper.getConnectedGatt(), mtu, status);
        }
    };

    public OTAManager(Context context) {
        super(context);
        
        BluetoothOTAConfigure bluetoothOption = new BluetoothOTAConfigure();
        //选择通讯方式
        bluetoothOption.setPriority(configHelper.isBleWay() ? 
            BluetoothOTAConfigure.PREFER_BLE : BluetoothOTAConfigure.PREFER_SPP);
        
        //是否需要自定义回连方式(默认不需要，如需要自定义回连方式，需要客户自行实现)
        bluetoothOption.setUseReconnect(
            configHelper.isUseCustomReConnectWay() && configHelper.isHidDevice());
        
        //是否启用设备认证流程(与固件工程师确认)
        bluetoothOption.setUseAuthDevice(configHelper.isUseDeviceAuth());
        
        //设置BLE的MTU
        bluetoothOption.setMtu(BluetoothConstant.BLE_MTU_MIN);
        
        //是否需要改变BLE的MTU
        bluetoothOption.setNeedChangeMtu(false);
        
        //是否启用杰理服务器(暂时不支持)
        bluetoothOption.setUseJLServer(false);

        //配置OTA参数
        configure(bluetoothOption);
        RcspAuth.setAuthTimeout(5000);
        bluetoothHelper.registerCallback(btEventCallback);
        
        if (bluetoothHelper.isConnected()) {
            onBtDeviceConnection(bluetoothHelper.getConnectedDevice(), StateCode.CONNECTION_OK);
            if (configHelper.isBleWay()) {
                onMtuChanged(
                    bluetoothHelper.getConnectedGatt(),
                    bluetoothHelper.getBleMtu() + 3,
                    BluetoothGatt.GATT_SUCCESS
                );
            }
        }
    }

    @Override
    public BluetoothDevice getConnectedDevice() {
        return bluetoothHelper.getConnectedDevice();
    }

    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {
        return bluetoothHelper.getConnectedGatt();
    }

    @Override
    public void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        //仅仅作为回连设备，回连设备现在仅支持BLE
        boolean result = bluetoothHelper.connectBleDevice(bluetoothDevice);
        if (!result) {
            onBtDeviceConnection(bluetoothDevice, StateCode.CONNECTION_FAILED);
        }
    }

    @Override
    public void disconnectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        bluetoothHelper.disconnectDevice(bluetoothDevice);
    }

    @Override
    public boolean sendDataToDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {
        JL_Log.d(TAG, "sendDataToDevice", "device : " + printDeviceInfo(bluetoothDevice) + "\n"
                + "data = [" + CHexConver.byte2HexStr(bytes) + "]");
        return bluetoothHelper.writeDataToDevice(bluetoothDevice, bytes);
    }

    @Override
    public void release() {
        super.release();
        bluetoothHelper.unregisterCallback(btEventCallback);
    }

    public void setReconnectAddr(String addr) {
        // setReconnectAddress(addr);
    }

    private String printDeviceInfo(BluetoothDevice device) {
        return BluetoothUtil.printBtDeviceInfo(context, device);
    }

    public void startLeScan(long l) {
        bluetoothHelper.startScan(l);
    }
}