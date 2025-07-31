package com.futurpals.flutter_jl_ota.otasdk.tool.file;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * FileManager
 * @author zqjasonZhong
 * @since 2025/4/9
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 文件管理器
 */
public class FileManager {

    private static Context context;
    
    private FileManager() {
        // 私有构造函数，防止实例化
    }

    public static void initialize(Context context) {
        FileManager.context = context.getApplicationContext();
    }

    public static boolean isUpgradeFile(String fileName) {
        return fileName.toLowerCase().endsWith(".ufw") || fileName.toLowerCase().endsWith(".bfu");
    }

    public static List<File> readUpgradeFile() {
        List<File> files = new ArrayList<>();
        String otaPath = context.getExternalFilesDir(null).getAbsolutePath() + "/upgrade";
        //读取私有空间下Upgrade文件夹的升级文件列表
        File folder = new File(otaPath);
        if (folder.exists() && folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile() && isUpgradeFile(file.getName())) {
                        if (!files.contains(file)) {
                            files.add(file);
                        }
                    }
                }
            }
        }
        return files;
    }
}