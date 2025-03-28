
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

typedef PermissionUtilOnAllowedCB = void Function(bool isAllow);

class PermissionUtil {
  static void preRequestPermissions(
      List<Permission> permissions, {

        required PermissionUtilOnAllowedCB onAllowed,
      }) async {
    if (await _checkAllPermissionsStatus(permissions)) {
      onAllowed(true);
    } else {
      // onAllowed(false);
      new_requestPermissions(permissions, onAllowed);

      // CommonToast.showToast("尚未授权的权限有：${permissions}.");
    }
  }



  // 权限名称映射
  static String _getPermissionName(Permission permission) {
    Map<Permission, String> permissionNames = {
      Permission.bluetooth: "蓝牙",
      Permission.location: "位置信息",
      Permission.locationAlways: "始终位置信息",
      Permission.microphone: "麦克风权限",
      Permission.storage: "存储权限",
      Permission.mediaLibrary: "访问媒体权限",
    };
    return permissionNames[permission] ?? "未知权限";
  }

  static Future<bool> _checkAllPermissionsStatus(
      List<Permission> permissions) async {
    bool status = true;
    for (var permission in permissions) {
      var permissionStatus = await permission.status;
      if (!permissionStatus.isGranted) {
        status = false;
        break;
      }
    }
    return status;
  }

  static Future new_requestPermissions(
      List<Permission> permissions, PermissionUtilOnAllowedCB onAllowed) async {
    var statuses = await permissions.request();
    bool allPermissionsGranted = statuses.values.every((status) => status.isGranted);

    // 调用回调，通知调用方权限请求结果
    onAllowed(allPermissionsGranted);

    if (allPermissionsGranted) {
      print("通过的权限有：${permissions.map(_getPermissionName)}.");
    } else {
      print("拒绝的权限有：${statuses.entries.where((entry) => !entry.value.isGranted).map((entry) => _getPermissionName(entry.key)).toList()}.");
    }
  }

  static Future _requestPermissions(
      List<Permission> permissions, PermissionUtilOnAllowedCB onAllowed) async {
    List<Future> requests = [];
    for (var permission in permissions) {
      if (await permission.status.isDenied) {
        // await permission.request();
        requests.add(permission.request());
      }
    }

    //等待请求完毕
    await Future.wait(requests);
    if (await _checkAllPermissionsStatus(permissions)) {
      onAllowed(true);
      print("通过的权限有：${permissions}.");
    } else {
      onAllowed(false);
      print("拒绝的权限有：${permissions}.");
    }
  }

  /// 判断权限
  static Future sn_requestPermissions(
      List<Permission> permissions, PermissionUtilOnAllowedCB onAllowed) async {
    _requestPermissions(permissions, onAllowed);
  }
}
