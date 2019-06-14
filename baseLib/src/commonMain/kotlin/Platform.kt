package tsl.baseLib

expect fun platformName(): String

internal expect fun outputLogMessage(level: LogLevel, msg: String)
internal expect fun reportException(e: Throwable)
internal expect fun getStackTrace(e: Throwable): String

expect object FileSystem {
    fun exists(path: String): Boolean
    fun isDirectory(path: String): Boolean
    fun isFile(path: String): Boolean

    fun delete(path: String): Boolean
    fun makeDirs(path: String): Boolean
}
