package com.futurpals.flutter_jl_ota.otasdk.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context

/**
 * DeviceUtil
 * @author zqjasonZhong
 * @since 2025/1/14
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 设备工具类
 */
object DeviceUtil {

    @SuppressLint("MissingPermission")
    fun getDeviceName(context: Context, device: BluetoothDevice?): String {
        if (device == null) return "N/A"
        val name = device.name ?: "N/A"
        return name.ifEmpty { "N/A" }
    }

    /**
     *
     */
    @SuppressLint("MissingPermission")
    fun getBtDeviceTypeString(context: Context, device: BluetoothDevice?): String {
        if (device == null) return ""
        return when (device.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "经典"
            BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "双"
            BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "未知"
            else -> ""
        }
    }
}