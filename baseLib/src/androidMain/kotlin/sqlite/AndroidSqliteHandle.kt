package tsl.baseLib.sqlite

import android.database.sqlite.*
import tsl.baseLib.Logger

class AndroidSqliteHandle(val db: SQLiteDatabase) : SqliteHandle {
    var modifiedCount: Int = 0
    var lastRowId: Long = 0

    override fun close() {
        try {
            db.close()
        } catch (e: SQLiteException) {
            Logger.exception(e, "error while closing sqlite database")
        }
    }

    override fun beginTransaction() {
        try {
            db.beginTransactionNonExclusive()
        } catch (e: SQLiteException) {
            throw convertException(e)
        }
    }

    override fun setTransactionSuccessful() {
        try {
            db.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            throw convertException(e)
        }
    }

    override fun endTransaction() {
        try {
            db.endTransaction()
        } catch (e: SQLiteException) {
            throw convertException(e)
        }
    }

    override fun prepare(query: String): SqliteStmt {
        return AndroidSqliteStmt(this, query, SqliteHandle.HINT_DEFAULT)
    }

    override fun prepare(query: String, hint: Int): SqliteStmt {
        return AndroidSqliteStmt(this, query, hint)
    }

    companion object {
        fun convertException(e: SQLiteException): SqliteException {
            return SqliteException(exceptionToErrorCode(e), e.message ?: "no message")
        }

        private fun exceptionToErrorCode(e: SQLiteException): Int {
            return when (e) {
                is SQLiteAbortException -> SqliteCode.ABORT
                is SQLiteAccessPermException -> SqliteCode.PERM
                is SQLiteBindOrColumnIndexOutOfRangeException -> SqliteCode.RANGE
                is SQLiteBlobTooBigException -> SqliteCode.TOOBIG
                is SQLiteCantOpenDatabaseException -> SqliteCode.CANTOPEN
                is SQLiteConstraintException -> SqliteCode.CONSTRAINT
                is SQLiteDatabaseCorruptException -> SqliteCode.CORRUPT
                is SQLiteDatabaseLockedException -> SqliteCode.BUSY
                is SQLiteDatatypeMismatchException -> SqliteCode.MISMATCH
                is SQLiteDiskIOException -> SqliteCode.IOERR
                is SQLiteDoneException -> SqliteCode.DONE
                is SQLiteFullException -> SqliteCode.FULL
                is SQLiteMisuseException -> SqliteCode.MISUSE
                is SQLiteOutOfMemoryException -> SqliteCode.NOMEM
                is SQLiteReadOnlyDatabaseException -> SqliteCode.READONLY
                is SQLiteTableLockedException -> SqliteCode.LOCKED
                else -> SqliteCode.ERROR
            }
        }
    }
}

actual fun createSqliteHandle(path: String, openFlags: Int): SqliteHandle {
    if (path == ":memory:") {
        val db = SQLiteDatabase.create(null)
        return AndroidSqliteHandle(db)
    }
    var flags = SQLiteDatabase.NO_LOCALIZED_COLLATORS
    if (openFlags and SqliteOpenFlags.WAL != 0) {
        flags = flags or SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING
    }
    if (openFlags and SqliteOpenFlags.READONLY != 0) {
        flags = flags or SQLiteDatabase.OPEN_READONLY
    }
    if (openFlags and SqliteOpenFlags.CREATE != 0) {
        flags = flags or SQLiteDatabase.CREATE_IF_NECESSARY
    }
    val db = SQLiteDatabase.openDatabase(path, null, flags)
    return AndroidSqliteHandle(db)
}
