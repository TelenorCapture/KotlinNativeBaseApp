package tsl.baseLib.logger

import android.util.Log

internal const val TAG = "tsl"

internal actual fun outputMessage(level: LogLevel, msg: String) {
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
