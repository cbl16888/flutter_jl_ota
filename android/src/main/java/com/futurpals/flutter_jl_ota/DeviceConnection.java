package com.futurpals.flutter_jl_ota;

import android.bluetooth.BluetoothDevice;

public class DeviceConnection {

    private final BluetoothDevice device;
    private int state;

    public DeviceConnection(BluetoothDevice device, int state) {
        this.device = device;
        this.state = state;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        return device != null ? device.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DeviceConnection that = (DeviceConnection) obj;

        return device != null ? device.equals(that.device) : that.device == null;
    }

    @Override
    public String toString() {
        return "DeviceConnection(device=" + device + ", state=" + state + ")";
    }
}