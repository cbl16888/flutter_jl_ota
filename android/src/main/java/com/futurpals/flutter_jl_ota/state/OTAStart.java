package com.futurpals.flutter_jl_ota.state;

import android.bluetooth.BluetoothDevice;

public class OTAStart extends OTAState {

    public OTAStart(BluetoothDevice device) {
        super(OTA_STATE_START, device);
    }

    @Override
    public String toString() {
        return "OTAStart()";
    }
}