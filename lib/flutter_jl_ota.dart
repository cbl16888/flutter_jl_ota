import 'package:flutter/services.dart';

import 'ota_service.dart';

class FlutterJlOta {
  // Future<String?> getPlatformVersion() {
  //   return FlutterJlOtaPlatform.instance.getPlatformVersion();
  // }

  // 开始扫描设备
  Future<bool> startScan() async {
    return OtaService.startScan();
  }

  // 连接设备
  static Future<bool> connectDevice(String uuid) async {
    return OtaService.connectDevice(uuid);
  }

  // 获取设备信息
  static Future<bool> getDeviceInfo() async {
    return OtaService.getDeviceInfo();
  }

  // 开始 OTA 升级
  static Future<bool> startOtaUpdate(String uuid, String filePath) async {
    return OtaService.startOtaUpdate(uuid, filePath);
  }

  // 取消 OTA 升级
  static Future<bool> cancelOtaUpdate() async {
    return OtaService.cancelOtaUpdate();
  }

  // 监听 OTA 进度和状态
  static void listenToOtaProgress(Function(int, String) onProgress) {
    OtaService.listenToOtaProgress(onProgress);
  }
}
