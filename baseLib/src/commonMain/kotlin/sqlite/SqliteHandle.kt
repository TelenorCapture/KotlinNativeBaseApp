package tsl.baseLib.sqlite

import kotlinx.io.core.Closeable

class SqliteException(val code: Int, message: String) : RuntimeException(message)

object SqliteCode {
    const val OK = 0
    const val ERROR = 1   /* SQL error or missing database */
    const val INTERNAL = 2   /* Internal logic error in SQLite */
    const val PERM = 3   /* Access permission denied */
    const val ABORT = 4   /* Callback routine requested an abort */
    const val BUSY = 5   /* The database file is locked */
    const val LOCKED = 6   /* A table in the database is locked */
    const val NOMEM = 7   /* A malloc() failed */
    const val READONLY = 8   /* Attempt to write a readonly database */
    const val INTERRUPT = 9   /* Operation terminated by sqlite3_interrupt()*/
    const val IOERR = 10   /* Some kind of disk I/O error occurred */
    const val CORRUPT = 11   /* The database disk image is malformed */
    const val NOTFOUND = 12   /* Unknown opcode in sqlite3_file_control() */
    const val FULL = 13   /* Insertion failed because database is full */
    const val CANTOPEN = 14   /* Unable to open the database file */
    const val PROTOCOL = 15   /* Database lock protocol error */
    const val EMPTY = 16   /* Database is empty */
    const val SCHEMA = 17   /* The database schema changed */
    const val TOOBIG = 18   /* String or BLOB exceeds size limit */
    const val CONSTRAINT = 19   /* Abort due to constraint violation */
    const val MISMATCH = 20   /* Data type mismatch */
    const val MISUSE = 21   /* Library used incorrectly */
    const val NOLFS = 22   /* Uses OS features not supported on host */
    const val AUTH = 23   /* Authorization denied */
    const val FORMAT = 24   /* Auxiliary database format error */
    const val RANGE = 25   /* 2nd parameter to sqlite3_bind out of range */
    const val NOTADB = 26   /* File opened that is not a database file */
    const val NOTICE = 27   /* Notifications from sqlite3_log() */
    const val WARNING = 28   /* Warnings from sqlite3_log() */
    const val ROW = 100  /* sqlite3_step() has another row ready */
    const val DONE = 101  /* sqlite3_step() has finished executing */
}

enum class QueryHint {
    REGULAR,
    SINGLE_LONG,
    SINGLE_TEXT
}

interface SqliteHandle : Closeable {
    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()

    fun prepare(query: String): SqliteStmt
    fun prepare(query: String, hint: QueryHint): SqliteStmt
}


object SqliteOpenFlags {
    const val WAL = 1
    const val READONLY = 1 shl 1
    const val CREATE = 1 shl 2
}

expect fun createSqliteHandle(path: String, openFlags: Int): SqliteHandle
