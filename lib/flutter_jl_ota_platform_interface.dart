import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_jl_ota_method_channel.dart';


abstract class FlutterJlOtaPlatform extends PlatformInterface {
  /// Constructs a FlutterJlOtaPlatform.
  FlutterJlOtaPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterJlOtaPlatform _instance = MethodChannelFlutterJlOta();

  /// The default instance of [FlutterJlOtaPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterJlOta].
  static FlutterJlOtaPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterJlOtaPlatform] when
  /// they register themselves.
  static set instance(FlutterJlOtaPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  //example
  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  void firmwareUpdate(
    String filePath,
    String uuid,
    String deviceName,
    Function(int result) callBack, {
    Map? otherParams,
  }) {
    throw UnimplementedError('firmwareUpdate() has not been implemented.');
  }
}
