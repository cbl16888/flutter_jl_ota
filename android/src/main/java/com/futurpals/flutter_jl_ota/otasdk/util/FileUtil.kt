package com.futurpals.flutter_jl_ota.otasdk.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.jieli.jl_bt_ota.constant.ErrorCode
import com.jieli.jl_bt_ota.interfaces.IActionCallback
import com.jieli.jl_bt_ota.model.OTAError
import com.jieli.jl_bt_ota.util.JL_Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * FileUtil
 * @author zqjasonZhong
 * @since 2024/9/20
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 文件工具类
 */
object FileUtil {

    private val TAG = FileUtil::class.simpleName

    /**
     * 杰理OTA文件夹
     */
    const val DIR_JL_OTA = "JieLiOTA"

    /**
     * 升级文件夹
     */
    const val DIR_UPGRADE = "upgrade"

    /**
     * 日志文件夹
     */
    const val DIR_LOGCAT = "logcat"

    /**
     * 创建文件夹路径
     *
     * @param context  上下文
     * @param dirNames 文件夹名称
     * @return 文件夹路径
     */
    fun createFilePath(context: Context?, vararg dirNames: String?): String {
        if (context == null || dirNames.isEmpty()) return ""
        var file = context.getExternalFilesDir(null)
        if (file == null || !file.exists()) return ""
        var filePath = StringBuilder(file.path)
        if (filePath.toString().endsWith("/")) {
            filePath = StringBuilder(filePath.substring(0, filePath.lastIndexOf("/")))
        }
        try {
            for (dirName in dirNames) {
                filePath.append("/").append(dirName)
                file = File(filePath.toString())
                if (!file.exists() || file.isFile) { //文件不存在
                    if (!file.mkdir()) {
                        JL_Log.w(
                            TAG, "createFilePath",
                            "create dir failed. filePath = $filePath"
                        )
                        break
                    }
                }
            }
        } catch (ignore: Exception) {
        }
        return filePath.toString()
    }

    /**
     * 是否分区存储
     */
    fun isScopedStorage(): Boolean =
        Environment.getExternalStorageDirectory().equals(Environment.getRootDirectory())

    /**
     * 是否打印文件
     *
     * @param filePath String 文件路径
     * @return Boolean 结果
     */
    fun isLogFile(filePath: String): Boolean {
        getFileNameByPath(filePath).let { name ->
            val index = name.lastIndexOf(".")
            if (index != -1 && index + 1 < name.length) {
                val suffix = name.substring(index + 1)
                if (suffix.equals("txt", true)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 从文件路径读取文件名
     *
     * @param filePath String 文件路径
     * @return String 文件名
     */
    fun getFileNameByPath(filePath: String): String {
        val index = filePath.lastIndexOf(File.separator)
        if (index == -1) return filePath
        if (index + 1 == filePath.length) return ""
        return filePath.substring(index + 1)
    }

    /**
     * 获取Download文件夹对应文件的文件夹路径
     *
     * @param isLogFile Boolean 是否打印文件
     * @return String 文件路径
     */
    fun getDownloadFolderPath(isLogFile: Boolean = false): String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + DIR_JL_OTA + File.separator + if (isLogFile) DIR_LOGCAT else DIR_UPGRADE

    /**
     * 获取文件的Download文件夹路径
     *
     * @param fileName String 文件名
     * @return String 对应的Download文件夹路径
     */
    fun getDownloadFilePath(fileName: String): String {
        return getDownloadFolderPath(isLogFile(fileName)) + File.separator + fileName
    }

    fun getDownloadDirectoryUri(): Uri =
        Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload")

    fun getUriByPath(context: Context, path: String): Uri? =
        if (path.isBlank()) null else getUriByFile(context, File(path))

    fun getUriByFile(context: Context, file: File): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context, context.packageName + ".provider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    /**
     * 删除文件/文件夹
     *
     * @param file File 文件/文件夹
     * @return Boolean 结果
     */
    fun deleteFile(file: File): Boolean {
        if (!file.exists()) return false
        if (file.isFile) {
            return file.delete()
        }
        val childFiles = file.listFiles()
        if (null == childFiles || childFiles.isEmpty()) {
            //空文件夹，直接删除
            return file.delete()
        }
        for (child in childFiles) {
            if (!deleteFile(child)) {
                //删除文件失败
                return false
            }
        }
        //已删除子文件，空文件夹，删除
        return file.delete()
    }

    private fun copyFile(
        context: Context,
        folderUri: Uri,
        filePath: String,
        callback: IActionCallback<String>
    ) {
        val file = File(filePath)
        if (!file.exists()) {
            callback.onError(OTAError.buildError(ErrorCode.SUB_ERR_FILE_NOT_FOUND))
            return
        }
        try {
            context.contentResolver.openOutputStream(folderUri)?.let { outputStream ->
                val input = FileInputStream(file)
                val buffer = ByteArray(1024)
                var readSize: Int
                while (input.read(buffer).also { readSize = it } != -1) {
                    outputStream.write(buffer, 0, readSize)
                }
                outputStream.close()
                input.close()
            }
            callback.onSuccess(filePath)
        } catch (e: IOException) {
            JL_Log.e(TAG, "copyFile", "exception : " + e.message)
            callback.onError(OTAError.buildError(ErrorCode.SUB_ERR_IO_EXCEPTION))
        }
    }
}