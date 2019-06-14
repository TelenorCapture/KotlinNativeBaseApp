package tsl.baseLib.sqlite

import interop.*
import kotlinx.cinterop.*
import tsl.baseLib.logger.Logger

internal fun makeException(db: CValuesRef<sqlite3>?): SqliteException {
    if (db == null) {
        return SqliteException(SQLITE_ERROR, "db is NULL (already closed)")
    }
    val code = sqlite3_errcode(db)
    val msg = sqlite3_errmsg(db)?.toKString() ?: "no message"
    return SqliteException(code, msg)
}

internal fun checkDb(db: CValuesRef<sqlite3>?, rc: Int) {
    if (rc != SQLITE_OK) {
        throw makeException(db)
    }
}

internal fun checkStmt(stmt: CValuesRef<sqlite3_stmt>?, rc: Int) {
    if (rc != SQLITE_OK) {
        throw makeException(sqlite3_db_handle(stmt))
    }
}

class IosSqliteHandle(var db: CValuesRef<sqlite3>?) : SqliteHandle {
    var transactionOk = false

    override fun close() {
        val rc = sqlite3_close(db)
        db = null
        if (rc != SQLITE_OK) {
            Logger.error("error code when closing SqliteHandle: $rc")
        }
    }

    override fun beginTransaction() {
        transactionOk = false
        val rc = sqlite3_exec(db, "BEGIN", null, null, null)
        checkDb(db, rc)
    }

    override fun setTransactionSuccessful() {
        transactionOk = true
    }

    override fun endTransaction() {
        if (transactionOk) {
            transactionOk = false
            val rc = sqlite3_exec(db, "COMMIT", null, null, null)
            checkDb(db, rc)
        } else {
            val rc = sqlite3_exec(db, "ROLLBACK", null, null, null)
            checkDb(db, rc)
        }
    }

    override fun prepare(query: String): SqliteStmt {
        memScoped {
            val stmtRef = nativeHeap.alloc<CPointerVar<sqlite3_stmt>>()
            val rc = sqlite3_prepare_v2(db, query, -1, stmtRef.ptr, null)
            checkDb(db, rc)
            return IosSqliteStmt(stmtRef.value)
        }
    }

    override fun prepare(query: String, hint: Int): SqliteStmt {
        return prepare(query)
    }
}

actual fun createSqliteHandle(path: String, openFlags: Int): SqliteHandle {
    memScoped {
        Logger.debug("dummyFunction result: " + dummyFunction())

        var db: CValuesRef<sqlite3>?
        var flags = 0
        if (openFlags and SqliteOpenFlags.READONLY != 0) {
            flags = flags or SQLITE_OPEN_READONLY
        } else {
            flags = flags or SQLITE_OPEN_READWRITE
        }
        if (openFlags and SqliteOpenFlags.CREATE != 0) {
            flags = flags or SQLITE_OPEN_CREATE
        }

        val dbRef = nativeHeap.alloc<CPointerVar<sqlite3>>()
        var rc = sqlite3_open_v2(path, dbRef.ptr, flags, null)
        db = dbRef.value
        checkDb(db, rc)

        rc = sqlite3_exec(db, "PRAGMA locking_mode=NORMAL", null, null, null)
        checkDb(db, rc)

        if (openFlags and SqliteOpenFlags.WAL != 0) {
            var result: String? = null
            val stmtRef = nativeHeap.alloc<CPointerVar<sqlite3_stmt>>()
            rc = sqlite3_prepare_v2(db, "PRAGMA journal_mode=WAL", -1, stmtRef.ptr, null)
            val stmt = stmtRef.value
            if (rc == SQLITE_OK) {
                rc = sqlite3_step(stmt)
                if (rc == SQLITE_ROW) {
                    result = sqlite3_column_text(stmt, 0)?.reinterpret<ByteVar>()?.toKString()
                }
            }
            if (stmt != null) {
                sqlite3_finalize(stmt)
            }
            if (result != null) {
                if (result == "wal") {
                    Logger.info("DB successfully opened in WAL mode!")
                } else {
                    Logger.error("Could not open DB in WAL mode! Selected mode: $result")
                }
            } else if (rc != SQLITE_ROW && rc != SQLITE_DONE) {
                throw makeException(db)
            } else {
                Logger.error("Could not open DB in WAL mode!")
            }
        }

        return IosSqliteHandle(db)
    }
}
