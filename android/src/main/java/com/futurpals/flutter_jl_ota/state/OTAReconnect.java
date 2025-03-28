package com.futurpals.flutter_jl_ota.state;

import android.bluetooth.BluetoothDevice;

public class OTAReconnect extends OTAState {

    private final String reconnectAddress;
    private final boolean isNewWay;

    public OTAReconnect(BluetoothDevice device, String reconnectAddress, boolean isNewWay) {
        super(OTA_STATE_RECONNECT, device);
        this.reconnectAddress = reconnectAddress;
        this.isNewWay = isNewWay;
    }

    public String getReconnectAddress() {
        return reconnectAddress;
    }

    public boolean isNewWay() {
        return isNewWay;
    }

    @Override
    public String toString() {
        return "OTAReconnect(reconnectAddress=" + reconnectAddress + ", isNewWay=" + isNewWay + ")";
    }
}