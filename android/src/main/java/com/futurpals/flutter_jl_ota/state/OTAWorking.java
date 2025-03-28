package com.futurpals.flutter_jl_ota.state;

import android.bluetooth.BluetoothDevice;

public class OTAWorking extends OTAState {

    private final int type;
    private final float progress;

    public OTAWorking(BluetoothDevice device, int type, float progress) {
        super(OTA_STATE_WORKING, device);
        this.type = type;
        this.progress = progress;
    }

    public int getType() {
        return type;
    }

    public float getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        return "OTAWorking(type=" + type + ", progress=" + progress + ")";
    }
}