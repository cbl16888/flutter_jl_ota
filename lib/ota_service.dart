import 'package:flutter/services.dart';

class OtaService {
  static const MethodChannel _channel = MethodChannel('flutter_jl_ota');

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
