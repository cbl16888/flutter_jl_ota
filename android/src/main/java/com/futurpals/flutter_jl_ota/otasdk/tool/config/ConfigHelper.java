package com.futurpals.flutter_jl_ota.otasdk.tool.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.IntRange;

import com.jieli.jl_bt_ota.constant.BluetoothConstant;
import com.futurpals.flutter_jl_ota.otasdk.data.constant.OtaConstant;

import java.util.UUID;

/**
 * @author zqjasonZhong
 * @since 2022/9/8
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 配置辅助类
 */
public class ConfigHelper {
    /**
     * 最新的协议对应的APP版本号
     */
    private static final int LATEST_POLICY_APP_VERSION = 10800;

    //download文件夹
    private static final String KEY_DOWNLOAD_URI = "download_uri";

    //同意协议版本号
    private static final String KEY_AGREE_POLICY_VERSION = "agree_policy_version";

    //通讯方式
    private static final String KEY_COMMUNICATION_WAY = "communication_way";

    //是否使用设备认证
    private static final String KEY_IS_USE_DEVICE_AUTH = "is_use_device_auth";

    //是否HID设备
    private static final String KEY_IS_HID_DEVICE = "is_hid_device";

    //是否使用自定义回连方式
    private static final String KEY_USE_CUSTOM_RECONNECT_WAY = "use_custom_reconnect_way";

    //BLE的MTU请求值
    private static final String KEY_BLE_MTU_VALUE = "ble_mtu_value";

    //是否启用SPP多通道
    private static final String KEY_SPP_MULTIPLE_CHANNEL = "spp_multiple_channel";

    //自定义SPP通道
    private static final String KEY_SPP_CUSTOM_UUID = "spp_custom_uuid";

    //是否自动测试OTA
    private static final String KEY_AUTO_TEST_OTA = "auto_test_ota";

    //自动化测试次数
    private static final String KEY_AUTO_TEST_COUNT = "auto_test_count";

    //是否自动测试OTA时，允许容错
    private static final String KEY_FAULT_TOLERANT = "fault_tolerant";

    //容错次数
    private static final String KEY_FAULT_TOLERANT_COUNT = "fault_tolerant_count";

    //扫描过滤参数
    private static final String KEY_SCAN_FILTER_STRING = "scan_filter_string";

    //开发者模式
    private static final String KEY_DEVELOP_MODE = "develop_mode";

    // 广播音箱模式
    private static final String KEY_BROADCAST_BOX = "broadcast_box_switch";

    @SuppressLint("StaticFieldLeak")
    private static volatile ConfigHelper instance;

    private final SharedPreferences preferences;

    private ConfigHelper(Context context) {
        preferences = context.getSharedPreferences("ota_config_data", Context.MODE_PRIVATE);
    }

    public static ConfigHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigHelper not initialized. Call initialize(context) first.");
        }
        return instance;
    }

    public static void initialize(Context context) {
        if (instance == null) {
            synchronized (ConfigHelper.class) {
                if (instance == null) {
                    instance = new ConfigHelper(context.getApplicationContext());
                }
            }
        }
    }

    public boolean isBleWay() {
        return preferences.getInt(
                KEY_COMMUNICATION_WAY,
                OtaConstant.CURRENT_PROTOCOL
        ) == OtaConstant.PROTOCOL_BLE;
    }

    public void setBleWay(boolean isBle) {
        int way = isBle ? OtaConstant.PROTOCOL_BLE : OtaConstant.PROTOCOL_SPP;
        preferences.edit().putInt(KEY_COMMUNICATION_WAY, way).apply();
    }

    public boolean isUseDeviceAuth() {
        return preferences.getBoolean(KEY_IS_USE_DEVICE_AUTH, OtaConstant.IS_NEED_DEVICE_AUTH);
    }

    public void setUseDeviceAuth(boolean isAuth) {
        preferences.edit().putBoolean(KEY_IS_USE_DEVICE_AUTH, isAuth).apply();
    }

    public boolean isHidDevice() {
        return preferences.getBoolean(KEY_IS_HID_DEVICE, OtaConstant.HID_DEVICE_WAY);
    }

    public void setHidDevice(boolean isHid) {
        preferences.edit().putBoolean(KEY_IS_HID_DEVICE, isHid).apply();
    }

    public boolean isUseCustomReConnectWay() {
        return preferences.getBoolean(
                KEY_USE_CUSTOM_RECONNECT_WAY,
                OtaConstant.NEED_CUSTOM_RECONNECT_WAY
        );
    }

    public void setUseCustomReConnectWay(boolean isCustom) {
        preferences.edit().putBoolean(KEY_USE_CUSTOM_RECONNECT_WAY, isCustom).apply();
    }

    public int getBleRequestMtu() {
        return preferences.getInt(KEY_BLE_MTU_VALUE, BluetoothConstant.BLE_MTU_MAX);
    }

    public void setBleRequestMtu(@IntRange(from = 20, to = 509) int mtu) {
        preferences.edit().putInt(KEY_BLE_MTU_VALUE, mtu).apply();
    }

    public boolean isUseMultiSppChannel() {
        return preferences.getBoolean(
                KEY_SPP_MULTIPLE_CHANNEL,
                OtaConstant.USE_SPP_MULTIPLE_CHANNEL
        );
    }

    public void setUseMultiSppChannel(boolean isUseMulti) {
        preferences.edit().putBoolean(KEY_SPP_MULTIPLE_CHANNEL, isUseMulti).apply();
    }

    public String getCustomSppChannel() {
        return preferences.getString(KEY_SPP_CUSTOM_UUID, OtaConstant.UUID_SPP.toString());
    }

    public void setCustomSppChannel(String uuid) {
        preferences.edit().putString(KEY_SPP_CUSTOM_UUID, uuid).apply();
    }

    public boolean isAutoTest() {
        return preferences.getBoolean(KEY_AUTO_TEST_OTA, OtaConstant.AUTO_TEST_OTA);
    }

    public void setAutoTest(boolean isAutoTest) {
        preferences.edit().putBoolean(KEY_AUTO_TEST_OTA, isAutoTest).apply();
    }

    public int getAutoTestCount() {
        return preferences.getInt(
                KEY_AUTO_TEST_COUNT,
                OtaConstant.AUTO_TEST_COUNT
        );
    }

    public void setAutoTestCount(int count) {
        if (!isAutoTest()) return;
        preferences.edit().putInt(KEY_AUTO_TEST_COUNT, count).apply();
    }

    public boolean isFaultTolerant() {
        return preferences.getBoolean(KEY_FAULT_TOLERANT, OtaConstant.AUTO_FAULT_TOLERANT);
    }

    public void setFaultTolerant(boolean isFaultTolerant) {
        preferences.edit().putBoolean(KEY_FAULT_TOLERANT, isFaultTolerant).apply();
    }

    public int getFaultTolerantCount() {
        return preferences.getInt(
                KEY_FAULT_TOLERANT_COUNT,
                OtaConstant.AUTO_FAULT_TOLERANT_COUNT
        );
    }

    public void setFaultTolerantCount(int count) {
        if (!isFaultTolerant()) return;
        preferences.edit().putInt(KEY_FAULT_TOLERANT_COUNT, count).apply();
    }

    public String getScanFilter() {
        return preferences.getString(KEY_SCAN_FILTER_STRING, "");
    }

    public void setScanFilter(String scanFilter) {
        preferences.edit().putString(KEY_SCAN_FILTER_STRING, scanFilter).apply();
    }

    public boolean isDevelopMode() {
        return preferences.getBoolean(KEY_DEVELOP_MODE, false);
    }

    public void setDevelopMode(boolean developMode) {
        preferences.edit().putBoolean(KEY_DEVELOP_MODE, developMode).apply();
    }

    public boolean isEnableBroadcastBox() {
        return preferences.getBoolean(KEY_BROADCAST_BOX, false);
    }

    public void enableBroadcastBox(boolean enable) {
        preferences.edit().putBoolean(KEY_BROADCAST_BOX, enable).apply();
    }
}