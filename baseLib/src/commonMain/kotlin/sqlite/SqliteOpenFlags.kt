package tsl.baseLib.sqlite

object SqliteOpenFlags {
    val WAL = 1
    val READONLY = 1 shl 1
    val CREATE = 1 shl 2
}
