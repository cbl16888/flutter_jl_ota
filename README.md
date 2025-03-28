# JL OTA Update Plugin for Flutter

A Flutter plugin for Over-The-Air (OTA) firmware updates targeting JL (JieLi) chipsets. Supports
Android and iOS platforms with progress callbacks and error handling.

|             | Android | iOS   |
|-------------|---------|-------|
| **Support** | SDK 16+ | 12.0+ |

---

## Setup

Integrated with the latest JL OTA plugin package

[Jieli OTA Android Official GitHub repository](https://github.com/Jieli-Tech/Android-JL_OTA)
[Jieli IOS Android Official GitHub repository](https://github.com/Jieli-Tech/Android-JL_OTA)

---

## Installation

```yaml
dependencies:
  flutter_jl_ota: ^0.0.2
```

## Example

<?code-excerpt "basic.dart (basic-example)"?>

```dart

String _platformVersion = 'Unknown';
final _JlOtaPlugin = FlutterJlOta();

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
  try {
    platformVersion =
        await _JlOtaPlugin.getPlatformVersion() ?? 'Unknown platform version';
  } on PlatformException {
    platformVersion = 'Failed to get platform version.';
  }

  // If the widget was removed from the tree while the asynchronous platform
  // message was in flight, we want to discard the reply rather than calling
  // setState to update our non-existent appearance.
  if (!mounted) return;

  PermissionUtil.preRequestPermissions([
    Permission.location,
    Permission.bluetoothScan,
    Permission.bluetoothConnect,
    Permission.bluetoothAdvertise
  ], onAllowed: (result) async {
    sn_testFirmwareUpgrade();
  });

  setState(() {
    _platformVersion = platformVersion;
  });
}

void sn_testFirmwareUpgrade() async {
  print("sn_log => ${'sn_testFirmwareUpgrade 执行了'}");

  if (Platform.isIOS) {
    try {
      String testOTA = await sn_moveFileToLib();
      _JlOtaPlugin.firmwareUpdate(
          testOTA,
          "7EAFC2B9-3EAF-9F9F-3292-F2770282F7D4",
          "DELI_MP502W_9E362B", (int result) {
        print("sn_log example 收到进度 ==>>> ${result}%");
      });
    } catch (e) {
      print("firmwareUpdate e=> ${e}");
    }
  } else {
    try {
      String testOTA = await sn_moveFileToLib();
      _JlOtaPlugin.firmwareUpdate(
          testOTA, "41:42:9F:D9:19:E5", "DELI_MP306W_921725", (int result) {
        print("!!!! test ==>>> ${result}%");
      });
    } catch (e) {
      print("firmwareUpdate e=> ${e}");
    }
  }
}

/// 调试用
static sn_moveFileToLib() async {
  String _fileName = 'update.ufw';

  String filePath = 'audio/${_fileName}';
  final ByteData data = await rootBundle.load(filePath);
  final List<int> bytes =
  data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
  String _filePath = await SNFilePathTool.sn_getFilePath(_fileName);
  File file = File(_filePath);
  await file.writeAsBytes(bytes);
  print("该文件bytes大小为 ${bytes.length}");
  return _filePath;
}

@override
Widget build(BuildContext context) {
  return MaterialApp(
    home: Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Text('Running on: $_platformVersion\n'),
      ),
    ),
  );
}
```
