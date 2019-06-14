package tsl.baseLib

import java.io.File

actual fun platformName(): String {
    return "Android"
}

actual object FileSystem {
    actual fun exists(path: String): Boolean = File(path).exists()
    actual fun isDirectory(path: String): Boolean = File(path).isDirectory
    actual fun isFile(path: String): Boolean = File(path).isFile

    actual fun delete(path: String): Boolean = File(path).delete()
    actual fun makeDirs(path: String): Boolean = File(path).mkdirs()
}
