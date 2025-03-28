package com.futurpals.flutter_jl_ota.tool;

import java.util.UUID;

/**
 * 常量声明
 *
 * @author zqjasonZhong
 * @date 2019/12/30
 */
public class OtaConstant {

    public static final UUID UUID_A2DP = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Ble协议
    public static final int PROTOCOL_BLE = 0;
    // Spp协议
    public static final int PROTOCOL_SPP = 1;

    public static final int CURRENT_PROTOCOL = PROTOCOL_BLE;

    // 是否使用设备认证
    public static final boolean IS_NEED_DEVICE_AUTH = true;

    // 是否HID设备连接
    public static final boolean HID_DEVICE_WAY = false;

    // 是否需要自定义连接方式
    public static final boolean NEED_CUSTOM_RECONNECT_WAY = false;

    // 是否使用SPP多通道连接
    public static final boolean USE_SPP_MULTIPLE_CHANNEL = false;

    // 是否使用自动化测试
    public static final boolean AUTO_TEST_OTA = false;
    // 自动化测试次数
    public static final int AUTO_TEST_COUNT = 30;

    // 是否自动化测试时允许容错
    public static final boolean AUTO_FAULT_TOLERANT = false;
    // 容错次数
    public static final int AUTO_FAULT_TOLERANT_COUNT = 1;

    // dir
    public static final String DIR_ROOT = "JieLiOTA";
    public static final String DIR_UPGRADE = "upgrade";
    public static final String DIR_LOGCAT = "logcat";

    public static final long SCAN_TIMEOUT = 16 * 1000L;
}