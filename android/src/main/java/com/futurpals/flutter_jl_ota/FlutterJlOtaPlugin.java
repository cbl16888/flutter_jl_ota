package com.futurpals.flutter_jl_ota;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.futurpals.flutter_jl_ota.tool.ConfigHelper;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IActionCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
//import com.example.jl_ota.tool.ConfigHelper;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterJlOtaPlugin
 */
public class FlutterJlOtaPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    private static final String TAG = "FlutterJlOtaPlugin";
    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private OtaManager otaManager;
    private static final long SCAN_TIMEOUT = 20 * 1000L; // 20秒，与 OtaManager 一致

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_jl_ota");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;

            case "startScan":
                if (otaManager == null) {
                    otaManager = new OtaManager(context, "", ""); // 初始空值，稍后通过 connectDevice 设置
                }
                otaManager.startScan();
                result.success(true);
                break;

            case "connectDevice":
                String uuid = call.argument("uuid");
                if (uuid != null && !uuid.isEmpty()) {
                    if (otaManager == null) {
                        otaManager = new OtaManager(context, uuid, "");
                    }
                    otaManager.connectDevice(uuid);
                    result.success(true);
                } else {
                    result.error("INVALID_UUID", "UUID is invalid", null);
                }
                break;

            case "getDeviceInfo":
                if (otaManager == null) {
                    result.error("NOT_INITIALIZED", "OTA manager not initialized", null);
                    return;
                }
                otaManager.getDeviceInfo(new IActionCallback<TargetInfoResponse>() {
                    @Override
                    public void onSuccess(TargetInfoResponse response) {
                        result.success(true); // 返回是否需要强制升级
                    }

                    @Override
                    public void onError(BaseError error) {
                        result.error("GET_DEVICE_INFO_FAILED", "Error getting device info: " + error.getMessage(), null);
                    }
                });
                break;

            case "startOtaUpdate":
                Map<String, Object> params = call.arguments();
                if (params != null && params.containsKey("uuid") && params.containsKey("filePath")) {
                    String otaUuid = (String) params.get("uuid");
                    String filePath = (String) params.get("filePath");
                    if (otaUuid != null && filePath != null) {
                        if (otaManager == null) {
                            otaManager = new OtaManager(context, otaUuid, "");
                        }
                        otaManager.startOtaUpdate(filePath, (progress, status) -> {
                            Map<String, Object> response = new HashMap<>();
                            response.put("progress", progress);
                            response.put("status", status != null ? status : "");
                            if (activity != null) {
                                activity.runOnUiThread(() -> channel.invokeMethod("otaProgress", response));
                            }
                        });
                        result.success(true);
                    } else {
                        result.error("INVALID_PARAMS", "UUID or filePath is invalid", null);
                    }
                } else {
                    result.error("INVALID_ARGUMENTS", "Arguments must be a map with uuid and filePath", null);
                }
                break;

            case "cancelOtaUpdate":
                if (otaManager != null) {
                    otaManager.cancelOtaUpdate(new IActionCallback<>() {
                        @Override
                        public void onSuccess(Integer status) {
                            result.success(status == 0);
                        }

                        @Override
                        public void onError(BaseError error) {
                            result.error("CANCEL_FAILED", "Failed to cancel OTA: " + error.getMessage(), null);
                        }
                    });
                } else {
                    result.error("NOT_INITIALIZED", "OTA manager not initialized", null);
                }
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    // OTA 进度回调接口
    interface OtaProgressCallback {
        void onProgress(int progress, String status);
    }
}

