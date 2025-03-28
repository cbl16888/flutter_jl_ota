
import 'package:flutter/services.dart';

import 'flutter_jl_ota_platform_interface.dart';


class FlutterJlOta {
  Future<String?> getPlatformVersion() {
    return FlutterJlOtaPlatform.instance.getPlatformVersion();
  }
  /// 固件升级
  void firmwareUpdate(String filePath, String uuid, String deviceName,
      Function(int result) callBack,
      {Map? otherParams}) async {
    try {
      FlutterJlOtaPlatform.instance.firmwareUpdate(
          filePath, uuid, deviceName, callBack,
          otherParams: otherParams);
    } on PlatformException catch (e) {
      print("固件升级异常${e}"); //e.toString()
    }
  }
}
