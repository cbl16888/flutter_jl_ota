import 'package:flutter/services.dart';

class OtaService {
  static const MethodChannel _channel = MethodChannel('flutter_jl_ota');

  // 开始扫描设备
  static Future<bool> startScan() async {
    try {
      final result = await _channel.invokeMethod('startScan');
      return result == true;
    } catch (e) {
      print('Error starting scan: $e');
      return false;
    }
  }

  // 连接设备
  static Future<bool> connectDevice(String uuid) async {
    try {
      final result = await _channel.invokeMethod('connectDevice', {
        'uuid': uuid,
      });
      return result == true;
    } catch (e) {
      print('Error connecting device: $e');
      return false;
    }
  }

  // 获取设备信息
  static Future<bool> getDeviceInfo() async {
    try {
      final result = await _channel.invokeMethod('getDeviceInfo');
      return result == true;
    } catch (e) {
      print('Error getting device info: $e');
      return false;
    }
  }

  // 开始 OTA 升级
  static Future<bool> startOtaUpdate(String uuid, String filePath) async {
    try {
      final result = await _channel.invokeMethod('startOtaUpdate', {
        'uuid': uuid,
        'filePath': filePath,
      });
      return result == true;
    } catch (e) {
      print('Error starting OTA: $e');
      return false;
    }
  }

  // 取消 OTA 升级
  static Future<bool> cancelOtaUpdate() async {
    try {
      final result = await _channel.invokeMethod('cancelOtaUpdate');
      return result == true;
    } catch (e) {
      print('Error cancelling OTA: $e');
      return false;
    }
  }

  // 监听 OTA 进度和状态
  static void listenToOtaProgress(Function(int, String) onProgress) {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'otaProgress') {
        final Map<dynamic, dynamic> response = call.arguments;
        final int progress = response['progress'] as int;
        final String status = response['status'] as String;
        onProgress(progress, status);
      }
      return null;
    });
  }
}
