package com.futurpals.flutter_jl_ota;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.futurpals.flutter_jl_ota.ble.BleManager;
import com.futurpals.flutter_jl_ota.ble.interfaces.BleEventCallback;
import com.futurpals.flutter_jl_ota.interfaces.OnWriteDataCallback;
import com.futurpals.flutter_jl_ota.tool.ConfigHelper;
import com.jieli.jl_bt_ota.constant.ErrorCode;
import com.jieli.jl_bt_ota.constant.StateCode;
import com.jieli.jl_bt_ota.impl.BluetoothOTAManager;
import com.jieli.jl_bt_ota.interfaces.BtEventCallback;
import com.jieli.jl_bt_ota.interfaces.IActionCallback;
import com.jieli.jl_bt_ota.interfaces.IUpgradeCallback;
import com.jieli.jl_bt_ota.model.BluetoothOTAConfigure;
import com.jieli.jl_bt_ota.model.base.BaseError;
import com.jieli.jl_bt_ota.model.response.TargetInfoResponse;
import com.jieli.jl_bt_ota.util.JL_Log;
//import com.example.jl_ota.tool.ConfigHelper;
import java.util.UUID;

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

    public MethodChannel channel;
    private static FlutterJlOtaPlugin instance;

    public static final String TAG = "JlOtaPlugin";

    //ota manager
    private OtaManager otaManager;

    public String otaFilePath;

    public Context context;
    public Activity activity;
    public MethodChannel send_channel;


    public int connectedCounts = 0;
    public BluetoothDevice mDevice = null;
public boolean canOtaButton = false;
    public JlOtaPlugin() {
    }

    public boolean isOtaRegister = false;

    // Get instance (Singleton)
    public static synchronized JlOtaPlugin getInstance() {
        if (instance == null) {
            instance = new JlOtaPlugin();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
//        JlOtaPlugin.getInstance().setContext(flutterPluginBinding.getApplicationContext());

        context = flutterPluginBinding.getApplicationContext();//context的初始化
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "jl_ota");
        channel.setMethodCallHandler(this);

        send_channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "send_channel");
        send_channel.setMethodCallHandler(this);
        ConfigHelper.initialize(context);

//
//    ConfigHelper.getInstance(context);
//        BleManager.getInstance();
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        activity = activityPluginBinding.getActivity();

    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
//        Log.e("call","call = " +call);
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("sn_updateFirmware")) {
            String remoteID = call.argument("uuid");
            String deviceName = call.argument("deviceName");
            otaFilePath = call.argument("filePath");
            Log.e("flutter传输","数据 from flutter => remoteID="+remoteID+",deviceName="+deviceName+",otaFilePath="+otaFilePath);
            initState(remoteID, deviceName);

        }
        else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        send_channel.setMethodCallHandler(null);


    }

    public void initState(String remoteID, String deviceName) {
        mDevice = getBluetoothDeviceById(remoteID);

//        Log.e(TAG, "设备：" + mDevice);

        if (context == null) {
            Log.e(TAG, "context == null");
            throw new IllegalStateException("Context is not initialized in JlOtaPlugin");
        }
        if (otaManager == null) {
            otaManager = new OtaManager(context, remoteID, deviceName);
        }

        BluetoothOTAConfigure bluetoothOption = BluetoothOTAConfigure.createDefault();


        bluetoothOption.setPriority(BluetoothOTAConfigure.PREFER_BLE) //请按照项目需要选择
                .setUseAuthDevice(true) //具体根据固件的配置选择
                .setBleIntervalMs(500) //默认是500毫秒
                .setTimeoutMs(3000) //超时时间
                .setMtu(500) //BLE底层通讯MTU值，会影响BLE传输数据的速率。建议用500 或者 270。该MTU值会使OTA库在BLE连接时改变MTU，所以用户SDK需要对此处理。
                .setNeedChangeMtu(false) //不需要调整MTU，建议客户连接时调整好BLE的MTU
                .setUseReconnect(true); //是否自定义回连方式，默认为false，走SDK默认回连方式，客户可以根据需求进行变更
        bluetoothOption.setFirmwareFilePath(otaFilePath); //设置本地存储OTA文件的路径


        otaManager.configure(bluetoothOption); //设置OTA参数
        otaManager.setOtaStatusCallback(new OtaStatusCallback() {
            @Override
            public void onCanStartOtaChanged(boolean canStartOta) {
                if (canStartOta) {
                    if(isOtaRegister) {
//                        Log.e(TAG,"重复 onCanStartOtaChanged"); 实际上没重复
                        return;
                    }
                    isOtaRegister=true;
                    otaManager.registerBluetoothCallback(new BtEventCallback() {
                        @Override
                        public void onError(BaseError baseError) {
                            Log.e("registerBluetoothCallback", "error -> " + baseError);
                            int code = baseError.getCode();
                            if (code == 0) {
//                                otaManager.release();
                                isOtaRegister=false;
                            }
                        }

                        @Override
                        public void onConnection(BluetoothDevice device, int status) {
                            connectedCounts++;
                            Log.e(TAG, "onConnection-> device:" + device + ",status==" + status);
                            //必须等待库回调连接成功才可以开始OTA操作
                            if (status == StateCode.CONNECTION_OK) {
                                if (otaManager.isOTA()) return; //如果已经在OTA流程，则不需要处理
                                Log.e(TAG, "start-> ota:" + device + ",status==" + status);
                                canOtaButton =true;
//                                OtaFirmware();
                                if(connectedCounts>=2){
                                    OtaFirmware();
                                }

                                //1.可以查询是否需要强制升级
                                otaManager.queryMandatoryUpdate(new IActionCallback<TargetInfoResponse>() {
                                    @Override
                                    public void onSuccess(TargetInfoResponse deviceInfo) {

                                        Log.e("queryMandatoryUpdate","强制升级 onSuccess");

//                                        OtaFirmware();
                                        //进行步骤2
                                    }

                                    @Override
                                    public void onError(BaseError baseError) {
                                        Log.e("queryMandatoryUpdate","强制升级error->"+baseError);

                                    }
                                });

                            }
                        }
                    });

                }
            }
        });

    }


    public void OtaFirmware() {

        otaManager.getBluetoothOption().setFirmwareFilePath(otaFilePath);

        //* 进行OTA升级，然后根据回调进行UI更新
        otaManager.startOTA(new IUpgradeCallback() {
            @Override
            public void onStartOTA() {
                connectedCounts=0;
                Log.e(TAG, "onStartOTA");
                //回调开始OTA
            }

            @Override
            public void onNeedReconnect(String addr, boolean isNewReconnectWay) {

                //如果客户设置了BluetoothOTAConfigure#setUseReconnect()为true，则需要在此处回调进行自定义回连设备流程
                if (otaManager.getBluetoothOption().isUseReconnect()) {
                    Log.e(TAG, "onNeedReconnect addr="+addr+",isNewReconnectWay="+isNewReconnectWay);

//                    //2-1 进行自定义回连流程
                    otaManager.reConnect(addr,isNewReconnectWay);
                }
            }

            @Override
            public void onProgress(int type, float progress) {
                Log.e(TAG, "升级进度 type="+type+",progress"+progress);
                int progressInt = (int) progress;
                if (type == 0) {

                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (send_channel != null) {
                                send_channel.invokeMethod("progress", progressInt);
                                Log.e(TAG, "原生升级进度:" + progressInt);
                            } else {
                                Log.e("channel", "channel null" + send_channel);
                            }

                        }
                    });
                }
                //回调OTA进度
                //type : 0 --- 下载loader  1 --- 升级固件
            }

            @Override
            public void onStopOTA() {
                //回调OTA升级完成
                send_channel.invokeMethod("progress", 100);

                Log.e(TAG, "onStopOTA onStopOTA onStopOTA");
                otaManager.release();
            }

            @Override
            public void onCancelOTA() {
                Log.e(TAG, "onCancelOTA onCancelOTA onCancelOTA");

                //回调OTA升级被取消
                //双备份OTA才允许OTA流程取消
            }

            @Override
            public void onError(BaseError error) {
                //回调OTA升级发生的错误事件
                Log.e(TAG, "ota error = " + error);
                int code = error.getCode();


            }
        });
        //...
        //3.OTA操作完成后，需要注销事件监听器和释放资源
//        otaManager.unregisterBluetoothCallback(this);
//        otaManager.release();
    }



    @SuppressLint("MissingPermission")
    private BluetoothDevice getBluetoothDeviceById(String remoteID) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice targetDevice = bluetoothAdapter.getRemoteDevice(remoteID);


        return targetDevice;
    }
}


