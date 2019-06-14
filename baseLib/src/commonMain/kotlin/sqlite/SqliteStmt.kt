package tsl.baseLib.sqlite

import kotlinx.io.core.Closeable

interface SqliteStmt : Closeable {
    val columnCount: Int

    val lastRowId: Long
    val lastChangeCount: Int
    fun reset()

    fun bindNull(index: Int)
    fun bindInt(index: Int, value: Int)
    fun bindLong(index: Int, value: Long)
    fun bindFloat(index: Int, value: Float)
    fun bindDouble(index: Int, value: Double)
    fun bindText(index: Int, value: String)
    fun bindBlob(index: Int, blob: ByteArray)
    fun clearBindings()

    fun step(): Boolean
    fun getColumnType(column: Int): Int
    fun getColumnName(column: Int): String?

    fun isNull(column: Int): Boolean
    fun isInteger(column: Int): Boolean
    fun isFloat(column: Int): Boolean
    fun isText(column: Int): Boolean
    fun isBlob(column: Int): Boolean

    fun getInt(column: Int): Int
    fun getLong(column: Int): Long
    fun getFloat(column: Int): Float
    fun getDouble(column: Int): Double
    fun getText(column: Int): String?
    fun getBlob(column: Int): ByteArray?
}
