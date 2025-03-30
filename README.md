**Languages:**

[![Chinese](https://img.shields.io/badge/Language-Chinese-blueviolet?style=for-the-badge)](README.md)
[![English](https://img.shields.io/badge/Language-English-blueviolet?style=for-the-badge)](README.en.md)

# Flutter 杰理OTA插件


针对JL（杰理）芯片的OTA固件更新的Flutter插件。支持带有进度回调和错误处理的Android和iOS平台，支持和flutter_blue_plus混用。

|          | Android | iOS   |
|----------|---------|-------|
| **版本支持** | SDK 21+ | 12.0+ |

---

## 安装

与最新的JL OTA插件包集成

* 安卓 SDK V1.9.2
* IOS SDK V2.3.1

[Android杰理官方github仓库地址](https://github.com/Jieli-Tech/Android-JL_OTA)
[IOS杰理官方github仓库地址](https://github.com/Jieli-Tech/iOS-JL_OTA)

---

## 下载

```yaml
dependencies:
  flutter_jl_ota: ^0.0.8
```

## 例子

* 将您的UFW固件放置在assets目录中
* 用您的实际设备Uuid替换设备Uuid

<?code-excerpt "basic.dart (basic-example)"?>

```dart
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
    if (!mounted) return;
    PermissionUtil.preRequestPermissions([
      Permission.location,
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.bluetoothAdvertise
    ], onAllowed: (result) async {});
  }

  void startOta() async {
    String deviceUuid = '2B3681AF-B077-297D-D291-FA4A908CE06A'; // 替换为实际 UUID
    print("flutter_ota_log => ${'startOta 执行了'}");
    String ufwPath = await moveFileToLib();
    await OtaService.startOtaUpdate(deviceUuid, ufwPath);

    // 监听进度和状态
    OtaService.listenToOtaProgress((progress, status) {
      print('OTA Progress: $progress%, Status: $status');
      if (status == 'Failed' || status == 'Success') {
        // 可选择取消监听或执行其他逻辑
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

```
