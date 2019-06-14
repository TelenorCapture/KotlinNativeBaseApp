package tsl.baseLib.logger

import platform.Foundation.NSLog

internal actual fun outputMessage(level: LogLevel, msg: String) {
    NSLog("$level: $msg")
}

internal actual fun reportException(e: Throwable) {

}

internal actual fun getStackTrace(e: Throwable): String {
    return ""
}
