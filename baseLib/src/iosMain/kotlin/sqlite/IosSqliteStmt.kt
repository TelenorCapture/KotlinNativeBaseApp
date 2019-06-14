package tsl.baseLib.sqlite

import interop.*
import kotlinx.cinterop.*
import platform.posix.memcpy
import tsl.baseLib.Logger

class IosSqliteStmt(var stmt: CValuesRef<sqlite3_stmt>?) : SqliteStmt {
    override val columnCount: Int
        get() = sqlite3_column_count(stmt)

    override val lastRowId: Long
        get() = sqlite3_last_insert_rowid(sqlite3_db_handle(stmt))

    override val lastChangeCount: Int
        get() = sqlite3_changes(sqlite3_db_handle(stmt))

    override fun reset() {
        sqlite3_reset(stmt)
        // ignore result code (because we always handle codes returned by step)
    }

    override fun bindNull(index: Int) {
        val rc = sqlite3_bind_null(stmt, index)
        checkStmt(stmt, rc)
    }

    override fun bindInt(index: Int, value: Int) {
        val rc = sqlite3_bind_int(stmt, index, value)
        checkStmt(stmt, rc)
    }

    override fun bindLong(index: Int, value: Long) {
        val rc = sqlite3_bind_int64(stmt, index, value)
        checkStmt(stmt, rc)
    }

    override fun bindFloat(index: Int, value: Float) {
        val rc = sqlite3_bind_double(stmt, index, value.toDouble())
        checkStmt(stmt, rc)
    }

    override fun bindDouble(index: Int, value: Double) {
        val rc = sqlite3_bind_double(stmt, index, value)
        checkStmt(stmt, rc)
    }

    override fun bindText(index: Int, value: String) {
        val rc = sqlite3_bind_text(stmt, index, value, -1, SQLITE_TRANSIENT)
        checkStmt(stmt, rc)
    }

    override fun bindBlob(index: Int, blob: ByteArray) {
        val rc = sqlite3_bind_blob(stmt, index, blob.refTo(0), blob.size, SQLITE_TRANSIENT)
        checkStmt(stmt, rc)
    }

    override fun clearBindings() {
        val rc = sqlite3_clear_bindings(stmt)
        checkStmt(stmt, rc)
    }

    override fun step(): Boolean {
        val rc = sqlite3_step(stmt)
        if (rc == SQLITE_ROW) {
            return true
        }
        if (rc == SQLITE_DONE) {
            return false
        }
        checkStmt(stmt, rc)
        return false
    }

    override fun getColumnType(column: Int): SqliteColumnType {
        return when (sqlite3_column_type(stmt, column)) {
            SQLITE_NULL -> SqliteColumnType.NULLTYPE
            SQLITE_INTEGER -> SqliteColumnType.INTEGER
            SQLITE_FLOAT -> SqliteColumnType.FLOAT
            SQLITE_TEXT -> SqliteColumnType.TEXT
            SQLITE_BLOB -> SqliteColumnType.BLOB
            else -> SqliteColumnType.NULLTYPE
        }
    }

    override fun getColumnName(column: Int): String? {
        return sqlite3_column_name(stmt, column)?.toKString()
    }

    override fun isNull(column: Int): Boolean {
        return sqlite3_column_type(stmt, column) == SQLITE_NULL
    }

    override fun isInteger(column: Int): Boolean {
        return sqlite3_column_type(stmt, column) == SQLITE_INTEGER
    }

    override fun isFloat(column: Int): Boolean {
        return sqlite3_column_type(stmt, column) == SQLITE_FLOAT
    }

    override fun isText(column: Int): Boolean {
        return sqlite3_column_type(stmt, column) == SQLITE_TEXT
    }

    override fun isBlob(column: Int): Boolean {
        return sqlite3_column_type(stmt, column) == SQLITE_BLOB
    }

    override fun getInt(column: Int): Int {
        return sqlite3_column_int(stmt, column)
    }

    override fun getLong(column: Int): Long {
        return sqlite3_column_int64(stmt, column)
    }

    override fun getFloat(column: Int): Float {
        return sqlite3_column_double(stmt, column).toFloat()
    }

    override fun getDouble(column: Int): Double {
        return sqlite3_column_double(stmt, column)
    }

    override fun getText(column: Int): String? {
        return sqlite3_column_text(stmt, column)?.reinterpret<ByteVar>()?.toKString()
    }

    override fun getBlob(column: Int): ByteArray? {
        val ptr = sqlite3_column_blob(stmt, column) ?: return null
        val size = sqlite3_column_bytes(stmt, column)
        val result = ByteArray(size)
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), ptr, size.toULong())
        }
        return result
    }

    override fun close() {
        val rc = sqlite3_finalize(stmt)
        stmt = null
        if (rc != SQLITE_OK) {
            Logger.error("error code when closing SqliteStmt: $rc")
        }
    }
}
