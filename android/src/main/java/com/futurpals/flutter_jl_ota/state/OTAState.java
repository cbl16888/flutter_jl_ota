package com.futurpals.flutter_jl_ota.state;

import android.bluetooth.BluetoothDevice;

public class OTAState {

    public static final int OTA_STATE_IDLE = 0;
    public static final int OTA_STATE_START = 1;
    public static final int OTA_STATE_WORKING = 2;
    public static final int OTA_STATE_RECONNECT = 3;

    private final int state;
    private BluetoothDevice device;

    public OTAState(int state, BluetoothDevice device) {
        this.state = state;
        this.device = device;
    }

    public int getState() {
        return state;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "OTAState(state=" + state + ", device=" + device + ")";
    }
}