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
  int progress = 0;
  String status = "--";

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    if (!mounted) return;
    PermissionUtil.preRequestPermissions([
      Permission.location,
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.bluetoothAdvertise
    ], onAllowed: (result) async {});
  }

  void startOta({bool isUpdate = true}) async {
    String deviceUuid = '680CD9BA-5C9B-4198-8FA5-0D35B7D2262F'; // 替换为实际 UUID
    if (Platform.isAndroid) {
      deviceUuid = "93:71:6C:2E:2E:2B";
    }
    print("flutter_ota_log => ${'startOta 执行了'}");
    var fileName = isUpdate ? 'update_bf_ptt_v1.0.5.ufw' : 'update_bf_ptt_v1.0.4.ufw';
    String ufwPath = await moveFileToLib(fileName);
    await OtaService.startOtaUpdate(deviceUuid, ufwPath);

    // 监听进度和状态
    OtaService.listenToOtaProgress((progress, status) {
      print('OTA Progress: $progress%, Status: $status');
      setState(() {
        this.progress = progress;
        this.status = status;
      });
      if (status == 'Failed' || status == 'Success') {
        // 可选择取消监听或执行其他逻辑
      }
    });
  }

  /// 调试用
  static moveFileToLib(String fileName) async {
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
            ),
            const SizedBox(height: 20,),
            Center(
              child: ElevatedButton(
                onPressed: () {
                  startOta(isUpdate: false);
                },
                child: const Text("downgrade"),
              ),
            ),
            const SizedBox(height: 20,),
            Text("$status:   $progress%")
          ],
        ),
      ),
    );
  }
}
