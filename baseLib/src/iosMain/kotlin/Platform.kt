package tsl.baseLib

import kotlinx.cinterop.*
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog

actual fun platformName(): String {
    return "iOS"
}


internal actual fun outputLogMessage(level: LogLevel, msg: String) {
    NSLog("$level: $msg")
}
internal actual fun reportException(e: Throwable) {
}
internal actual fun getStackTrace(e: Throwable): String {
    return ""
}


actual object FileSystem {
    actual fun exists(path: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }
    actual fun isDirectory(path: String): Boolean {
        memScoped {
            val isDir = nativeHeap.alloc<BooleanVar>()
            if (!NSFileManager.defaultManager.fileExistsAtPath(path, isDir.ptr)) {
                return false
            }
            return isDir.value
        }
    }
    actual fun isFile(path: String): Boolean {
        memScoped {
            val isDir = nativeHeap.alloc<BooleanVar>()
            if (!NSFileManager.defaultManager.fileExistsAtPath(path, isDir.ptr)) {
                return false
            }
            return !isDir.value
        }
    }

    actual fun delete(path: String): Boolean {
        return NSFileManager.defaultManager.removeItemAtPath(path, null)
    }
    actual fun makeDirs(path: String): Boolean {
        return NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
    }
}
