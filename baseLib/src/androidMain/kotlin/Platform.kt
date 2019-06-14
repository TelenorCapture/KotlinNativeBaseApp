package tsl.baseLib

import android.util.Log
import java.io.File

actual fun platformName(): String {
    return "Android"
}


internal const val TAG = "tsl"
internal actual fun outputLogMessage(level: LogLevel, msg: String) {
    when (level) {
        LogLevel.DEBUG -> Log.d(TAG, msg)
        LogLevel.INFO -> Log.i(TAG, msg)
        LogLevel.WARNING -> Log.w(TAG, msg)
        LogLevel.ERROR -> Log.e(TAG, msg)
    }
}
internal actual fun reportException(e: Throwable) {
}
internal actual fun getStackTrace(e: Throwable): String {
    return ""
}


actual object FileSystem {
    actual fun exists(path: String): Boolean = File(path).exists()
    actual fun isDirectory(path: String): Boolean = File(path).isDirectory
    actual fun isFile(path: String): Boolean = File(path).isFile

    actual fun delete(path: String): Boolean = File(path).delete()
    actual fun makeDirs(path: String): Boolean = File(path).mkdirs()
}
