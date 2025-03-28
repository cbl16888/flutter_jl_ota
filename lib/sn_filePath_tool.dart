import 'dart:io';

import 'package:path_provider/path_provider.dart';

class SNFilePathTool {
  /// 获取文件路径(自动拼接)
  static Future<String> sn_getFilePath(
    String sn_fileName, {
    bool sn_isNeedAccoutIsolation = false,
  }) async {
    String _result = sn_fileName;

    if (Platform.isIOS) {
      Directory tempDir = await getLibraryDirectory();
      _result = '${tempDir.path}';
    } else {
      List<Directory>? appDocDirs = await getExternalCacheDirectories();
      Directory? tempDir = appDocDirs?.first;
      _result = '${tempDir?.path}';
    }

    // /// 需要账号隔离
    // if (sn_isNeedAccoutIsolation == true) {
    //   // 当前用户手机号
    //   String _currentUserPhoneNumber =
    //       await SNSharedPreferrencesTool.getUserPhoneNumber();
    //   _result += '/';
    //   _result += _currentUserPhoneNumber;
    // }

    /// 传空或者"则返回文件夹路径
    if (sn_fileName.isNotEmpty && sn_fileName != "") {
      _result += '/';
      _result += sn_fileName;
    }

    // print("sn_log => sn_getFilePath ${_result}");

    return _result;
  }
}
