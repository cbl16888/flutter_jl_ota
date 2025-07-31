package com.futurpals.flutter_jl_ota.otasdk.tool.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.jieli.jl_bt_ota.constant.JL_Constant;
import com.jieli.jl_bt_ota.model.BleScanMessage;
import com.jieli.jl_bt_ota.tool.DeviceReConnectManager;
import com.jieli.jl_bt_ota.util.BluetoothUtil;
import com.jieli.jl_bt_ota.util.JL_Log;
import com.jieli.jl_bt_ota.util.ParseDataUtil;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.BleManager;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.interfaces.BleEventCallback;
import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.model.BleScanInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zqjasonZhong
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 回连辅助类
 * @since 2022/12/9
 */
public class ReConnectHelper {
    private static final String TAG = ReConnectHelper.class.getSimpleName();
    private static final long RECONNECT_TIMEOUT = DeviceReConnectManager.RECONNECT_TIMEOUT; //65秒
    private static final long SCAN_TIMEOUT = 20 * 1000L; //20秒
    private static final long FAILED_DELAY = 3 * 1000L;
    private static final int MSG_RECONNECT_TIMEOUT = 0x01;
    private static final int MSG_PROCESS_TASK = 0x02;

    private final Context mContext;
    private final BleManager mBtManager;
    private final List<ReconnectParam> mParams = new ArrayList<>();
    private final Map<String, BleScanMessage> mBleAdvCache = new HashMap<>();

    private final Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECONNECT_TIMEOUT:
                    stopBtScan();
                    mParams.clear();
                    break;
                case MSG_PROCESS_TASK:
                    processReconnectTask();
                    break;
                default:
                    if (msg.obj instanceof String) {
                        String address = (String) msg.obj;
                        removeParam(address);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private final BleEventCallback bleEventCallback = new BleEventCallback() {
        @Override
        public void onAdapterChange(boolean bEnabled) {
            if (!isReconnecting()) return;
            if (bEnabled) {
                JL_Log.i(TAG, "onAdapterChange : bluetooth is on, try to start le scan.");
                mUIHandler.sendEmptyMessage(MSG_PROCESS_TASK);
            }
        }

        @Override
        public void onDiscoveryBleChange(boolean bStart) {
            if (!isReconnecting()) return;
            boolean isConnecting = mBtManager.isConnecting();
            JL_Log.i(TAG, "onDiscoveryBleChange : " + bStart + ", isConnecting = " + isConnecting);
            if (!bStart) {
                if (!isConnecting) {
                    mUIHandler.sendEmptyMessage(MSG_PROCESS_TASK);
                }
            }
        }

        @Override
        public void onDiscoveryBle(BluetoothDevice device, BleScanInfo bleScanMessage) {
            if (!isReconnecting() || null == device) return;
            BleScanMessage advMsg = ParseDataUtil.parseOTAFlagFilterWithBroad(
                    bleScanMessage.getRawData(),
                    JL_Constant.OTA_IDENTIFY
            );
            if (advMsg != null) {
                mBleAdvCache.put(device.getAddress(), advMsg);
                JL_Log.d(TAG, "onDiscoveryBle : put data in map.");
            }
            boolean isReconnectDevice = isReconnectDevice(device, advMsg);
            JL_Log.d(
                    TAG,
                    "onDiscoveryBle : " + device + ", isReconnectDevice = " + isReconnectDevice + ", " + advMsg
            );
            if (isReconnectDevice) {
                stopBtScan();
                ReconnectParam param = getCacheParam(device.getAddress());
                if (null != param) param.setConnectAddress(device.getAddress());
                JL_Log.d(TAG, "onDiscoveryBle : " + device + ", param = " + param);
                mBtManager.connectBleDevice(device);
            }
        }

        @Override
        public void onBleConnection(BluetoothDevice device, int status) {
            if (!isReconnecting() || null == device) return;
            BleScanMessage advMsg = mBleAdvCache.get(device.getAddress());
            boolean isReconnectDevice = isReconnectDevice(device, advMsg);
            if (!isReconnectDevice) return;
            JL_Log.i(TAG, "onBleConnection : " + device + ", status = " + status + ", " + advMsg);
            if (status == BluetoothProfile.STATE_CONNECTED) {
                JL_Log.w(TAG, "onBleConnection : removeParam >>> " + device.getAddress());
                removeParam(device.getAddress());
            } else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                JL_Log.i(TAG, "-onConnection- resume reconnect task.");
                mUIHandler.sendEmptyMessage(MSG_PROCESS_TASK);
            }
        }
    };

    public ReConnectHelper(Context mContext, BleManager mBtManager) {
        this.mContext = mContext;
        this.mBtManager = mBtManager;
        mBtManager.registerBleEventCallback(bleEventCallback);
    }

    public void release() {
        mParams.clear();
        mBleAdvCache.clear();
        mUIHandler.removeCallbacksAndMessages(null);
        mBtManager.unregisterBleEventCallback(bleEventCallback);
    }

    public boolean isReconnecting() {
        return mUIHandler.hasMessages(MSG_RECONNECT_TIMEOUT);
    }

    public boolean isMatchAddress(String srcAddress, String checkAddress) {
        ReconnectParam param = getCacheParam(srcAddress);
        return !(null == param || !BluetoothAdapter.checkBluetoothAddress(checkAddress)) && 
                (checkAddress.equals(param.getDeviceAddress()) || checkAddress.equals(param.getConnectAddress()));
    }

    public boolean putParam(ReconnectParam param) {
        if (null == param) return false;
        if (!mParams.contains(param)) {
            if (mParams.add(param)) {
                //添加任务超时
                mUIHandler.sendEmptyMessageDelayed(mParams.hashCode(), RECONNECT_TIMEOUT);
                if (!isReconnecting()) {
                    mUIHandler.sendMessageDelayed(
                            mUIHandler.obtainMessage(
                                    MSG_RECONNECT_TIMEOUT,
                                    param.getDeviceAddress()
                            ), RECONNECT_TIMEOUT + 10 * 1000
                    );
                    mUIHandler.sendEmptyMessage(MSG_PROCESS_TASK);
                }
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void stopBtScan() {
        mBtManager.stopLeScan();
    }

    private ReconnectParam getCacheParam(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) return null;
        BleScanMessage advMsg = mBleAdvCache.get(address);
        for (ReconnectParam param : new ArrayList<>(mParams)) {
            if (address.equals(param.getDeviceAddress()) || 
                    (advMsg != null && param.getDeviceAddress().equals(advMsg.getOldBleAddress()))) {
                return param;
            }
        }
        return null;
    }

    private void removeParam(String address) {
        ReconnectParam param = getCacheParam(address);
        if (param == null) return;
        if (mParams.remove(param)) {
            mUIHandler.removeMessages(param.hashCode());
            if (mParams.isEmpty()) {
                mUIHandler.removeMessages(MSG_RECONNECT_TIMEOUT);
                return;
            }
        }
        mUIHandler.sendEmptyMessage(MSG_PROCESS_TASK);
    }

    private void processReconnectTask() {
        if (mBtManager.isBleScanning()) {
            mUIHandler.sendEmptyMessageDelayed(MSG_PROCESS_TASK, FAILED_DELAY);
            return;
        }
        BluetoothDevice connectedDevice = getSystemConnectedDevice();
        if (null != connectedDevice) {
            ReconnectParam param = getCacheParam(connectedDevice.getAddress());
            if (null != param) param.setConnectAddress(connectedDevice.getAddress());
            mBtManager.connectBleDevice(connectedDevice);
            return;
        }
        if (!mBtManager.startLeScan(SCAN_TIMEOUT)) {
            JL_Log.i(TAG, "processReconnectTask : start Le scan failed.");
            mUIHandler.sendEmptyMessageDelayed(MSG_PROCESS_TASK, FAILED_DELAY);
        }
    }

    private BluetoothDevice getSystemConnectedDevice() {
        List<BluetoothDevice> list = BluetoothUtil.getSystemConnectedBtDeviceList(mContext);
        if (null == list || list.isEmpty()) return null;
        for (BluetoothDevice device : list) {
            if (isReconnectDevice(device, null)) {
                return device;
            }
        }
        return null;
    }

    private boolean isReconnectDevice(BluetoothDevice device, BleScanMessage message) {
        if (null == device || mParams.isEmpty()) return false;
        boolean ret = false;
        for (ReconnectParam param : new ArrayList<>(mParams)) {
            ret = param.isUseNewADV() && message != null && message.isOTA() ?
                    param.getDeviceAddress().equals(message.getOldBleAddress()) :
                    param.getDeviceAddress().equals(device.getAddress());
            if (ret) break;
        }
        return ret;
    }

    public static class ReconnectParam {
        private final String deviceAddress;
        private final boolean isUseNewADV;
        private String connectAddress;

        public ReconnectParam(String deviceAddress, boolean isUseNewADV) {
            this.deviceAddress = deviceAddress;
            this.isUseNewADV = isUseNewADV;
        }

        public String getDeviceAddress() {
            return deviceAddress;
        }

        public boolean isUseNewADV() {
            return isUseNewADV;
        }

        public String getConnectAddress() {
            return connectAddress;
        }

        public void setConnectAddress(String connectAddress) {
            this.connectAddress = connectAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReconnectParam that = (ReconnectParam) o;
            return isUseNewADV == that.isUseNewADV && deviceAddress.equals(that.deviceAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(deviceAddress, isUseNewADV);
        }

        @Override
        public String toString() {
            return "ReconnectParam{" +
                    "deviceAddress='" + deviceAddress + '\'' +
                    ", isUseNewADV=" + isUseNewADV +
                    ", connectAddress='" + connectAddress + '\'' +
                    '}';
        }
    }
}