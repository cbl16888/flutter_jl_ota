package com.futurpals.flutter_jl_ota.otasdk.data.constant;

import java.util.Locale;
import java.util.UUID;

public class OtaConstant {

    public static final UUID UUID_A2DP = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb");

    public static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    
    public static final int PROTOCOL_BLE = 0;
    public static final int PROTOCOL_SPP = 1;
    public static final int CURRENT_PROTOCOL = PROTOCOL_BLE;
    public static final boolean IS_NEED_DEVICE_AUTH = true;
    public static final boolean HID_DEVICE_WAY = false;
    public static final boolean NEED_CUSTOM_RECONNECT_WAY = false;
    public static final boolean USE_SPP_MULTIPLE_CHANNEL = false;
    public static final boolean AUTO_TEST_OTA = false;
    public static final int AUTO_TEST_COUNT = 30;
    public static final boolean AUTO_FAULT_TOLERANT = false;
    public static final int AUTO_FAULT_TOLERANT_COUNT = 1;

    public static final String DIR_ROOT = "JieLiOTA";
    public static final String DIR_UPGRADE = "upgrade";
    public static final String DIR_LOGCAT = "logcat";

    public static final long SCAN_TIMEOUT = 30 * 1000L;

    public static String formatString(String format, Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }
}