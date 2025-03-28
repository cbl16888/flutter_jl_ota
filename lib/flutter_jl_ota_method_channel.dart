import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_jl_ota/sn_firmware_updateTool.dart';

import 'flutter_jl_ota_platform_interface.dart';

/// An implementation of [FlutterJlOtaPlatform] that uses method channels.
class MethodChannelFlutterJlOta extends FlutterJlOtaPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_jl_ota');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  /// 固件升级
  @override
  void firmwareUpdate(
    String filePath,
    String uuid,
    String deviceName,
    Function(int result) callBack, {
    Map? otherParams,
  }) async {
    SNFirmwareUpdateTool().sn_updateAction(
      filePath,
      uuid,
      deviceName,
      callBack,
      otherParams: otherParams,
    );
  }
}
