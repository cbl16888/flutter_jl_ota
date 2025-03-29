import 'dart:io';

import 'package:path_provider/path_provider.dart';

class OtaPathUtil {
  /// 获取文件路径(自动拼接)
  static Future<String> getFilePath(
    String fileName, {
    bool isNeedAccountIsolation = false,
  }) async {
    String result = fileName;

    if (Platform.isIOS) {
      Directory tempDir = await getLibraryDirectory();
      result = tempDir.path;
    } else {
      List<Directory>? appDocDirs = await getExternalCacheDirectories();
      Directory? tempDir = appDocDirs?.first;
      result = '${tempDir?.path}';
    }

    if (fileName.isNotEmpty && fileName != "") {
      result += '/';
      result += fileName;
    }
    return result;
  }
}
