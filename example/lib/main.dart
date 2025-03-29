import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_jl_ota/flutter_jl_ota.dart';
import 'package:flutter_jl_ota/ota_service.dart';
import 'package:permission_handler/permission_handler.dart';

import 'ota_path_util.dart';
import 'permission_util.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  // String _platformVersion = 'Unknown';
  final otaPlugin = FlutterJlOta();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    print("sn_log => ${'sn_testFirmwareUpgrade 执行了1'}");

    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    // try {
    //   platformVersion =
    //       await _JlOtaPlugin.getPlatformVersion() ?? 'Unknown platform version';
    // } on PlatformException {
    //   platformVersion = 'Failed to get platform version.';
    // }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    PermissionUtil.preRequestPermissions([
      Permission.location,
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.bluetoothAdvertise
    ], onAllowed: (result) async {});

    // setState(() {
    //   _platformVersion = platformVersion;
    // });
  }

  // void sn_testFirmwareUpgrade() async {
  //   print("sn_log => ${'sn_testFirmwareUpgrade 执行了'}");
  //
  //   if (Platform.isIOS) {
  //     try {
  //       String testOTA = await sn_moveFileToLib();
  //       _JlOtaPlugin.firmwareUpdate(
  //           testOTA, "2B3681AF-B077-297D-D291-FA4A908CE06A", "TFY_BLE",
  //           (int result) {
  //         print("sn_log example 收到进度 ==>>> ${result}%");
  //       });
  //     } catch (e) {
  //       print("firmwareUpdate e=> ${e}");
  //     }
  //   } else {
  //     try {
  //       String testOTA = await sn_moveFileToLib();
  //       _JlOtaPlugin.firmwareUpdate(testOTA, "41:42:9F:D9:19:E5", "TFY_BLE",
  //           (int result) {
  //         print("!!!! test ==>>> ${result}%");
  //       });
  //     } catch (e) {
  //       print("firmwareUpdate e=> ${e}");
  //     }
  //   }
  // }

  // /// 调试用
  // static sn_moveFileToLib() async {
  //   String _fileName = 'update.ufw';
  //
  //   String filePath = 'assets/${_fileName}';
  //   final ByteData data = await rootBundle.load(filePath);
  //   final List<int> bytes =
  //       data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
  //   String _filePath = await SNFilePathTool.sn_getFilePath(_fileName);
  //   File file = File(_filePath);
  //   await file.writeAsBytes(bytes);
  //   print("该文件bytes大小为 ${bytes.length}");
  //   return _filePath;
  // }

  void startOta() async {
    print("flutter_ota_log => ${'startOta 执行了'}");
    String ufwPath = await moveFileToLib();
    OtaService.startOtaUpdate(
      "2B3681AF-B077-297D-D291-FA4A908CE06A", // 替换为实际设备 UUID
      ufwPath, // 替换为实际固件路径
    ).then((success) {
      if (success) {
        print('OTA update started successfully');
      } else {
        print('Failed to start OTA update');
      }
    });
  }

  /// 调试用
  static moveFileToLib() async {
    String fileName = 'update.ufw';

    String filePath = 'assets/$fileName';
    final ByteData data = await rootBundle.load(filePath);
    final List<int> bytes =
        data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
    String libPath = await OtaPathUtil.getFilePath(fileName);
    File file = File(libPath);
    await file.writeAsBytes(bytes);
    print("该文件bytes大小为 ${bytes.length}");
    return libPath;
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Center(
            //   child: Text('Running on: $_platformVersion\n'),
            // ),
            Center(
              child: ElevatedButton(
                onPressed: () {
                  startOta();
                },
                child: const Text("upgrade"),
              ),
            )
          ],
        ),
      ),
    );
  }
}
