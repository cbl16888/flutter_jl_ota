package com.futurpals.flutter_jl_ota;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.futurpals.flutter_jl_ota.ble.BleManager;
import com.futurpals.flutter_jl_ota.ble.interfaces.BleEventCallback;
import com.futurpals.flutter_jl_ota.ble.interfaces.OnWriteDataCallback;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.util.BluetoothUtil;
import com.jieli.jl_bt_ota.util.CHexConver;
import com.jieli.jl_bt_ota.util.JL_Log;

import java.util.UUID;

class OtaManager extends BluetoothOTAManager {
    private final Context context;
    private final BleManager bleManager;
    public String OTA_TAG = "OTA_MANAGER";
    public String mac = "";
    public String deviceName = "";
    private static final long SCAN_TIMEOUT = 20 * 1000L; // 20秒
    private OtaStatusCallback otaStatusCallback; // 添加回调接口
    boolean canStartOta = false;

    public OtaManager(Context context, String m_mac, String d_deviceName) {
        super(context);

        Log.e(OTA_TAG, " run OtaManager");

        this.context = context;
        if (context == null) {
            Log.e(OTA_TAG, " run OtaManager && context == null");

            throw new IllegalStateException("Context is required to initialize OtaManager");
        }
        this.mac = m_mac;
        this.deviceName = d_deviceName;
        this.bleManager = new BleManager(context, mac, deviceName);
//        bleManager.getInstance(context,mac,deviceName);
        bleManager.startLeScan(SCAN_TIMEOUT);

//      bleManager.connectBleDevice();
//        Log.e(OTA_TAG, " run OtaManager 1");

        //TODO:用户通过自行实现的连接库对象完成传递设备连接状态和接收到的数据
        bleManager.registerBleEventCallback(new BleEventCallback() {
            @Override
            public void onBleConnection(BluetoothDevice device, int status) {
                super.onBleConnection(device, status);
                //TODO: 注意：转变成OTA库的连接状态
                //注意: 需要正确传入设备连接状态，不要重复传入相同状态， 连接中-已连接-断开 或者 已连接-断开
                int connectStatus = changeConnectStatus(status);
                Log.e(TAG, "connectStatus==" + connectStatus);//如果是1 就是连接成功
                //传递设备的连接状态

                onBtDeviceConnection(device, connectStatus);


                if (connectStatus == 1) {

                    setCanStartOta(true);

                }
            }

            @Override
            public void onBleDataNotification(BluetoothDevice device, UUID serviceUuid, UUID characteristicsUuid, byte[] data) {
                super.onBleDataNotification(device, serviceUuid, characteristicsUuid, data);
                //传递设备的接收数据
                onReceiveDeviceData(device, data);
            }

            @Override
            public void onBleDataBlockChanged(BluetoothDevice device, int block, int status) {
                super.onBleDataBlockChanged(device, block, status);
                //传递BLE的MTU改变
                //注意: 非必要实现，建议客户在设备连接上时进行MTU协商
                onMtuChanged(getConnectedBluetoothGatt(), block, status);
            }
        });


//        Log.e(OTA_TAG, " run OtaManager 2");

    }

    /**
     * 获取已连接的蓝牙设备
     * <p>
     * 注意：1. 是通讯方式对应的蓝牙设备对象<br/>
     * 2. 实时反映设备的连接状态，设备已连接时有值，设备断开时返回null
     * </p>
     */
    @Override
    public BluetoothDevice getConnectedDevice() {
        //TODO:用户自行实现
        return bleManager.getConnectedBtDevice();
    }

    //获取状态
    // 设置 canStartOta，并调用回调接口
    private void setCanStartOta(boolean canStart) {
        if (canStartOta) {
            Log.e("setCanStartOta", "return ~ canStartOta==" + canStartOta);
            return;
        }
        Log.e("setCanStartOta", "回调-》设置canStart==" + canStart);
        if (this.canStartOta != canStart) {
            this.canStartOta = canStart;
            if (otaStatusCallback != null) {
                otaStatusCallback.onCanStartOtaChanged(canStart);
            }
        }
    }

    // 设置回调接口的方法
    public void setOtaStatusCallback(OtaStatusCallback callback) {
        this.otaStatusCallback = callback;
    }


    /**
     * 获取已连接的BluetoothGatt对象
     * <p>
     * 若选择BLE方式OTA，需要实现此方法。反之，SPP方式不需要实现
     * </p>
     */
    @Override
    public BluetoothGatt getConnectedBluetoothGatt() {
        //TODO:用户自行实现
        return bleManager.getConnectedBtGatt(getConnectedDevice());
    }

    /**
     * 连接蓝牙设备
     * <p>
     * 注意:1. 目前的回连方式都是回连BLE设备，只需要实现回连设备的BLE
     * 2. 该方法用于设备回连过程，如果客户是双备份OTA或者自行实现回连流程，不需要实现
     * </p>
     *
     * @param device 通讯方式的蓝牙设备
     */
    @Override
    public void connectBluetoothDevice(BluetoothDevice device) {
        //TODO:用户自行实现连接设备
        if (bleManager.connectBleDevice(device)) {
            Log.e(TAG, "connectBluetoothDevice -> " + bleManager.connectBleDevice(device));
        }
        bleManager.connectBleDevice(device);
    }

    public void reConnect(String address, boolean isUseAdv) {
        // Step 0. 转换成目标地址，比如地址+1

        bleManager.reconnectDevice(address, isUseAdv);

    }

    /**
     * 断开蓝牙设备的连接
     *
     * @param device 通讯方式的蓝牙设备
     */
    @Override
    public void disconnectBluetoothDevice(BluetoothDevice device) {
        //TODO:用户自行实现断开设备

        bleManager.disconnectBleDevice(device);
    }

    /**
     * 发送数据到蓝牙设备
     * <p>
     * 注意: 1. 需要实现可靠的大数据传输<br/>
     * 1.1 如果是BLE发送数据，需要根据MTU进行分包，然后队列式发数，确保数据发出<br/>
     * 1.2 如果是BLE发送数据 而且 协商MTU大于128， 建议发送MTU = 协商MTU - 6， 进行边缘保护
     * 2. 该方法在发送数据时回调，发送的数据是组装好的RCSP命令。一般长度在[10, 525]
     * </p>
     *
     * @param device 已连接的蓝牙设备
     * @param data   数据包
     * @return 操作结果
     */
    @Override
    public boolean sendDataToDevice(BluetoothDevice device, byte[] data) {
        bleManager.writeDataByBleAsync(device, BleManager.BLE_UUID_SERVICE, BleManager.BLE_UUID_WRITE, data, new OnWriteDataCallback() {
            @Override
            public void onBleResult(BluetoothDevice device, UUID serviceUUID, UUID characteristicUUID, boolean result, byte[] data) {
                //返回结果
            }
        });
        //也可以阻塞等待结果
        return true;
    }

    @Override
    public void release() {
        super.release();
        bleManager.destroy();
        canStartOta = false;
        mac = "";
        deviceName = "";

        Log.e(TAG, "ota manager destroy,bleManager destroy");
    }

    private int changeConnectStatus(int status) {
        int changeStatus = StateCode.CONNECTION_DISCONNECT;
        switch (status) {
            case BluetoothProfile.STATE_DISCONNECTED:
            case BluetoothProfile.STATE_DISCONNECTING: {
                changeStatus = StateCode.CONNECTION_DISCONNECT;
                break;
            }
            case BluetoothProfile.STATE_CONNECTED:
                changeStatus = StateCode.CONNECTION_OK;
                break;
            case BluetoothProfile.STATE_CONNECTING:
                changeStatus = StateCode.CONNECTION_CONNECTING;
                break;
        }

        return changeStatus;
    }
}