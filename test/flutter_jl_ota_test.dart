import 'package:flutter_jl_ota/flutter_jl_ota.dart';
import 'package:flutter_jl_ota/flutter_jl_ota_method_channel.dart';
import 'package:flutter_jl_ota/flutter_jl_ota_platform_interface.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterJlOtaPlatform
    with MockPlatformInterfaceMixin
    implements FlutterJlOtaPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  void firmwareUpdate(String filePath, String uuid, String deviceName, Function(int result) callBack, {Map? otherParams}) {
    // TODO: implement firmwareUpdate
  }
}

void main() {
  final FlutterJlOtaPlatform initialPlatform = FlutterJlOtaPlatform.instance;

  test('$MethodChannelFlutterJlOta is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterJlOta>());
  });

  // test('getPlatformVersion', () async {
  //   FlutterJlOta jlOtaPlugin = FlutterJlOta();
  //   MockFlutterJlOtaPlatform fakePlatform = MockFlutterJlOtaPlatform();
  //   FlutterJlOtaPlatform.instance = fakePlatform;
  //
  //   expect(await jlOtaPlugin.getPlatformVersion(), '42');
  // });
}
