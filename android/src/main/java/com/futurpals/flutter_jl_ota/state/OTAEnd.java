package com.futurpals.flutter_jl_ota.state;

import android.bluetooth.BluetoothDevice;

public class OTAEnd extends OTAState {

    private final int code;
    private String message;

    public OTAEnd(BluetoothDevice device, int code, String message) {
        super(OTA_STATE_IDLE, device);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "OTAEnd(code=" + code + ", message=" + message + ")";
    }
}