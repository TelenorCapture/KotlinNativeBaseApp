package tsl.baseLib.sqlite

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDoneException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteStatement
import tsl.baseLib.Logger

import java.util.Arrays

class AndroidSqliteStmt(handle: AndroidSqliteHandle, query: String, hint: Int) : SqliteStmt {
    private val type: Int
    private val handle: AndroidSqliteHandle
    private val db: SQLiteDatabase
    private val query: String

    private val stmt: SQLiteStatement?
    private var stmtDone: Boolean = false
    private var simpleLongResult: Long = 0
    private var simpleTextResult: String? = null

    private var selectCursor: Cursor? = null
    private var selectParams: Array<String?>? = null
    private var selectParamCount: Int = 0

    private var columnCountHack: Int = 0

    override val columnCount: Int
        get() = if (type == CURSOR_SELECT) {
                    selectCursor!!.columnCount
                } else columnCountHack

    override val lastRowId: Long
        get() = handle.lastRowId

    override val lastChangeCount: Int
        get() = handle.modifiedCount

    init {
        try {
            this.type = when {
                query.regionMatches(0, "SELECT", 0, 6, ignoreCase = true) -> when (hint) {
                    SqliteHandle.HINT_SINGLE_LONG -> SIMPLE_LONG_SELECT
                    SqliteHandle.HINT_SINGLE_TEXT -> SIMPLE_TEXT_SELECT
                    else -> CURSOR_SELECT
                }
                query.regionMatches(0, "INSERT", 0, 6, ignoreCase = true) -> INSERT
                query.regionMatches(0, "UPDATE", 0, 6, ignoreCase = true) -> UPDATE_DELETE
                query.regionMatches(0, "DELETE", 0, 6, ignoreCase = true) -> UPDATE_DELETE
                query.regionMatches(0, "REPLACE", 0, 7, ignoreCase = true) -> INSERT
                query.regionMatches(0, "WITH", 0, 4, ignoreCase = true) -> CURSOR_SELECT
                query.regionMatches(0, "PRAGMA", 0, 6, ignoreCase = true) -> if (query.contains("=")) {
                    UPDATE_DELETE
                } else {
                    CURSOR_SELECT
                }
                else -> OTHER
            }

            this.handle = handle
            this.db = handle.db
            this.query = query

            if (type == CURSOR_SELECT) {
                selectParams = arrayOfNulls(8)
                stmt = null
            } else {
                stmt = db.compileStatement(query)
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun close() {
        if (selectCursor != null) {
            selectCursor!!.close()
        }
        try {
            stmt?.close()
        } catch (e: SQLiteException) {
            Logger.exception(e, "error while closing sqlite statement")
        }
    }

    override fun reset() {
        columnCountHack = 0
        stmtDone = false
        simpleLongResult = 0
        simpleTextResult = null
        if (selectCursor != null) {
            selectCursor!!.close()
            selectCursor = null
        }
    }

    override fun bindNull(index: Int) {
        try {
            if (type == CURSOR_SELECT) {
                setCursorSelectParam(index, null)
            } else {
                stmt!!.bindNull(index)
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun bindInt(index: Int, value: Int) {
        try {
            if (type == CURSOR_SELECT) {
                setCursorSelectParam(index, Integer.toString(value))
            } else {
                stmt!!.bindLong(index, value.toLong())
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun bindLong(index: Int, value: Long) {
        try {
            if (type == CURSOR_SELECT) {
                setCursorSelectParam(index, java.lang.Long.toString(value))
            } else {
                stmt!!.bindLong(index, value)
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun bindFloat(index: Int, value: Float) {
        try {
            if (type == CURSOR_SELECT) {
                setCursorSelectParam(index, java.lang.Float.toString(value))
            } else {
                stmt!!.bindDouble(index, value.toDouble())
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun bindDouble(index: Int, value: Double) {
        try {
            if (type == CURSOR_SELECT) {
                setCursorSelectParam(index, java.lang.Double.toString(value))
            } else {
                stmt!!.bindDouble(index, value)
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun bindText(index: Int, value: String) {
        try {
            if (type == CURSOR_SELECT) {
                setCursorSelectParam(index, value)
            } else {
                stmt!!.bindString(index, value)
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun bindBlob(index: Int, blob: ByteArray) {
        try {
            if (type == CURSOR_SELECT) {
                throw RuntimeException("blob argument not supported for cursor select")
            } else {
                stmt!!.bindBlob(index, blob)
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun clearBindings() {
        if (type == CURSOR_SELECT) {
            Arrays.fill(selectParams, null)
            selectParamCount = 0
        } else {
            stmt!!.clearBindings()
        }
    }

    override fun step(): Boolean {
        try {
            when (type) {
                CURSOR_SELECT -> {
                    if (selectCursor == null) {
                        selectCursor = db.rawQuery(query, Arrays.copyOfRange(selectParams, 0, selectParamCount))
                    }
                    return selectCursor!!.moveToNext()
                }
                SIMPLE_LONG_SELECT -> {
                    if (!stmtDone) {
                        try {
                            simpleLongResult = stmt!!.simpleQueryForLong()
                            columnCountHack = 1
                            return true
                        } catch (e: SQLiteDoneException) {
                            columnCountHack = 0
                        }

                        stmtDone = true
                    }
                    return false
                }
                SIMPLE_TEXT_SELECT -> {
                    if (!stmtDone) {
                        try {
                            simpleTextResult = stmt!!.simpleQueryForString()
                            columnCountHack = 1
                            return true
                        } catch (e: SQLiteDoneException) {
                            columnCountHack = 0
                        }

                        stmtDone = true
                    }
                    return false
                }
                OTHER -> {
                    if (!stmtDone) {
                        stmt!!.execute()
                        stmtDone = true
                    }
                    return false
                }
                INSERT -> {
                    if (!stmtDone) {
                        handle.lastRowId = stmt!!.executeInsert()
                        stmtDone = true
                    }
                    return false
                }
                UPDATE_DELETE -> {
                    if (!stmtDone) {
                        handle.modifiedCount = stmt!!.executeUpdateDelete()
                        stmtDone = true
                    }
                    return false
                }
                else -> throw RuntimeException("unexpected internal statement type: $type")
            }
        } catch (e: SQLiteException) {
            throw AndroidSqliteHandle.convertException(e)
        }
    }

    override fun getColumnType(column: Int): SqliteColumnType {
        return when (type) {
            CURSOR_SELECT -> when (selectCursor!!.getType(column)) {
                Cursor.FIELD_TYPE_NULL -> SqliteColumnType.NULLTYPE
                Cursor.FIELD_TYPE_INTEGER -> SqliteColumnType.INTEGER
                Cursor.FIELD_TYPE_FLOAT -> SqliteColumnType.FLOAT
                Cursor.FIELD_TYPE_STRING -> SqliteColumnType.TEXT
                Cursor.FIELD_TYPE_BLOB -> SqliteColumnType.BLOB
                else -> SqliteColumnType.NULLTYPE
            }
            SIMPLE_LONG_SELECT -> SqliteColumnType.INTEGER
            SIMPLE_TEXT_SELECT -> SqliteColumnType.TEXT
            else -> SqliteColumnType.NULLTYPE
        }
    }

    override fun getColumnName(column: Int): String? {
        return if (type == CURSOR_SELECT) {
            selectCursor!!.getColumnName(column)
        } else null
    }

    override fun isNull(column: Int): Boolean {
        return getColumnType(column) == SqliteColumnType.NULLTYPE
    }

    override fun isInteger(column: Int): Boolean {
        return getColumnType(column) == SqliteColumnType.INTEGER
    }

    override fun isFloat(column: Int): Boolean {
        return getColumnType(column) == SqliteColumnType.FLOAT
    }

    override fun isText(column: Int): Boolean {
        return getColumnType(column) == SqliteColumnType.TEXT
    }

    override fun isBlob(column: Int): Boolean {
        return getColumnType(column) == SqliteColumnType.BLOB
    }

    override fun getInt(column: Int): Int {
        when (type) {
            CURSOR_SELECT -> return selectCursor!!.getInt(column)
            SIMPLE_LONG_SELECT -> {
                if (column != 0) {
                    return 0
                }
                if (simpleLongResult < Integer.MIN_VALUE || simpleLongResult > Integer.MAX_VALUE) {
                    throw RuntimeException("overflow while reading long value as int")
                }
                return simpleLongResult.toInt()
            }
            else -> return 0
        }
    }

    override fun getLong(column: Int): Long {
        return when (type) {
            CURSOR_SELECT -> selectCursor!!.getLong(column)
            SIMPLE_LONG_SELECT -> {
                if (column != 0) {
                    0
                } else simpleLongResult
            }
            else -> 0
        }
    }

    override fun getFloat(column: Int): Float {
        return if (type == CURSOR_SELECT) {
            selectCursor!!.getFloat(column)
        } else 0f
    }

    override fun getDouble(column: Int): Double {
        return if (type == CURSOR_SELECT) {
            selectCursor!!.getDouble(column)
        } else 0.0
    }

    override fun getText(column: Int): String? {
        return when (type) {
            CURSOR_SELECT -> selectCursor!!.getString(column)
            SIMPLE_LONG_SELECT -> {
                if (column != 0) {
                    null
                } else simpleTextResult
            }
            else -> null
        }
    }

    override fun getBlob(column: Int): ByteArray? {
        return if (type == CURSOR_SELECT) {
            selectCursor!!.getBlob(column)
        } else null
    }

    private fun setCursorSelectParam(index: Int, str: String?) {
        var i = index
        if (i <= 0) {
            throw SqliteException(SqliteCode.RANGE, "bind index out of range: $i")
        }
        --i
        while (i >= selectParams!!.size) {
            selectParams = selectParams!!.copyOf(selectParams!!.size * 2)
        }
        selectParams!![i] = str
        if (index >= selectParamCount) {
            selectParamCount = i + 1
        }
    }

    companion object {
        const val OTHER = 0
        const val CURSOR_SELECT = 1
        const val SIMPLE_LONG_SELECT = 2
        const val SIMPLE_TEXT_SELECT = 3
        const val INSERT = 4
        const val UPDATE_DELETE = 5
    }
}
