package tsl.baseLib

import kotlinx.cinterop.*
import platform.Foundation.NSFileManager

actual fun platformName(): String {
    return "iOS"
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
