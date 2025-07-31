package com.futurpals.flutter_jl_ota.otasdk.tool.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Looper;

import com.futurpals.flutter_jl_ota.otasdk.tool.ota.ble.model.BleScanInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author zqjasonZhong
 * @since 2022/9/14
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 蓝牙事件回调辅助类
 */
public class BTEventCbHelper extends OnBTEventCallback {
    private final List<OnBTEventCallback> callbacks = new ArrayList<>();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public void registerCallback(OnBTEventCallback callback) {
        if (callbacks.contains(callback)) return;
        callbacks.add(callback);
    }

    public void unregisterCallback(OnBTEventCallback callback) {
        if (callbacks.isEmpty()) return;
        callbacks.remove(callback);
    }

    public void release() {
        callbacks.clear();
        uiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAdapterChange(boolean bEnabled) {
        callbackEvent(new CallbackImpl<OnBTEventCallback>() {
            @Override
            public void onCallback(OnBTEventCallback callback) {
                callback.onAdapterChange(bEnabled);
            }
        });
    }

    @Override
    public void onDiscoveryChange(boolean bStart, int scanType) {
        callbackEvent(new CallbackImpl<OnBTEventCallback>() {
            @Override
            public void onCallback(OnBTEventCallback callback) {
                callback.onDiscoveryChange(bStart, scanType);
            }
        });
    }

    @Override
    public void onDiscovery(BluetoothDevice device, BleScanInfo bleScanMessage) {
        callbackEvent(new CallbackImpl<OnBTEventCallback>() {
            @Override
            public void onCallback(OnBTEventCallback callback) {
                callback.onDiscovery(device, bleScanMessage);
            }
        });
    }

    @Override
    public void onDeviceConnection(BluetoothDevice device, int way, int status) {
        callbackEvent(new CallbackImpl<OnBTEventCallback>() {
            @Override
            public void onCallback(OnBTEventCallback callback) {
                callback.onDeviceConnection(device, way, status);
            }
        });
    }

    @Override
    public void onReceiveData(BluetoothDevice device, int way, UUID uuid, byte[] data) {
        callbackEvent(new CallbackImpl<OnBTEventCallback>() {
            @Override
            public void onCallback(OnBTEventCallback callback) {
                callback.onReceiveData(device, way, uuid, data);
            }
        });
    }

    @Override
    public void onBleMtuChange(BluetoothDevice device, int mtu, int status) {
        callbackEvent(new CallbackImpl<OnBTEventCallback>() {
            @Override
            public void onCallback(OnBTEventCallback callback) {
                callback.onBleMtuChange(device, mtu, status);
            }
        });
    }

    private void callbackEvent(CallbackImpl<OnBTEventCallback> impl) {
        if (null == impl) return;
        Runnable runnable = new CallbackRunnable<>(callbacks, impl);
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            runnable.run();
        } else {
            uiHandler.post(runnable);
        }
    }

    interface CallbackImpl<T> {
        void onCallback(T callback);
    }

    static class CallbackRunnable<T> implements Runnable {
        private final List<T> callbacks;
        private final CallbackImpl<T> impl;

        CallbackRunnable(List<T> callbacks, CallbackImpl<T> impl) {
            this.callbacks = callbacks;
            this.impl = impl;
        }

        @Override
        public void run() {
            if (callbacks.isEmpty() || impl == null) return;
            List<T> temp = new ArrayList<>(callbacks);
            for (T t : temp) {
                impl.onCallback(t);
            }
        }
    }
}