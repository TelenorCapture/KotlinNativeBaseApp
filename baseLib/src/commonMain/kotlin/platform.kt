package tsl.baseLib

expect fun platformName(): String

expect object FileSystem {
    fun exists(path: String): Boolean
    fun isDirectory(path: String): Boolean
    fun isFile(path: String): Boolean

    fun delete(path: String): Boolean
    fun makeDirs(path: String): Boolean
}
