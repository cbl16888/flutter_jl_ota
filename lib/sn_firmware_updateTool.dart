// 固件更新工具类

// import 'dart:io';
import 'dart:io';
import 'package:flutter/services.dart';

class SNFirmwareUpdateTool {
  static SNFirmwareUpdateTool? _instance;

  static const platform = MethodChannel("flutter_jl_ota");
  final MethodChannel _sendChannel = MethodChannel('send_channel');

  // 私有的命名构造函数
  SNFirmwareUpdateTool._internal();

  factory SNFirmwareUpdateTool() {
    _instance ??= SNFirmwareUpdateTool._internal();

    return _instance!;
  }

  /// 更新动作
  void sn_updateAction(
    String filePath,
    String uuid,
    String deviceName,
    Function(int result) callBack, {
    Map? otherParams,
  }) {
    print("执行sn_updateAction，接收参数$filePath$uuid$deviceName");
    if (Platform.isIOS) {
      platform.setMethodCallHandler((MethodCall call) async {
        switch (call.method) {
          case 'progress':
            // print("flutter progress ios =>  ${call.arguments}");
            callBack(call.arguments);
            break;
        }
      });
    } else {
      _sendChannel.setMethodCallHandler((MethodCall call) async {
        switch (call.method) {
          case 'progress':
            int progress = call.arguments;
            print("flutter progress =>  ${call.arguments}"); // 通过
            callBack(progress);
                      break;
        }
      });
    }

    Map params = {};
    params["filePath"] = filePath;
    params["uuid"] = uuid;
    params["deviceName"] = deviceName;

    snWriteData(params);
  }

  /// 写入数据
  void snWriteData(Map params) async {
    // 桥接原生进行更新
    // 文件路径
    // String _filePath = await sn_moveFileToLib();
    // String _filePath = filePath;

    // Map _params = {};
    // _params["filePath"] = _filePath;
    // _params["uuid"] = SNBLETool().device?.remoteId.str ?? "";
    // _params["deviceName"] = SNBLETool().device?.platformName ?? "";

    dynamic result = await platform.invokeMethod("sn_updateFirmware", params);
    print("sn_log => sn_updateFirmware result => $result");
  }

  // /// 调试用
  // Future<String> sn_moveFileToLib() async {
  //   // String filePath = 'audio/zlftest001.opus';
  //   String _fileName = 'ota.bin';
  //   String filePath = 'audio/${_fileName}';
  //   final ByteData data = await rootBundle.load(filePath);
  //   final List<int> bytes =
  //       data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
  //   String _filePath = await SNFilePathTool.sn_getFilePath(_fileName);
  //   File file = File(_filePath);
  //   await file.writeAsBytes(bytes);
  //   // print("该文件bytes大小为 ${bytes.length}");
  //   return _filePath;
  // }
}
