import 'package:flutter/services.dart';

import 'flutter_jl_ota_platform_interface.dart';
import 'ota_service.dart';

class FlutterJlOta {
  // Future<String?> getPlatformVersion() {
  //   return FlutterJlOtaPlatform.instance.getPlatformVersion();
  // }

  /// 固件升级
  void startOta(
    String filePath,
    String uuid,
    String deviceName,
    Function(int result) callBack, {
    Map? otherParams,
  }) async {
    OtaService.startOtaUpdate(
      uuid, // 替换为实际设备 UUID
      filePath, // 替换为实际固件路径
    ).then((success) {
      if (success) {
        print('OTA update started successfully');
      } else {
        print('Failed to start OTA update');
      }
    });
  }

  void listenToOtaProgress() async {
    OtaService.listenToOtaProgress((progress, status) {
      print('OTA Progress: $progress%, Status: $status');
    });
  }
}
