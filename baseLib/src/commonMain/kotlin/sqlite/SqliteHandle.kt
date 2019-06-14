package tsl.baseLib.sqlite

import kotlinx.io.core.Closeable

interface SqliteHandle : Closeable {
    fun beginTransaction()
    fun setTransactionSuccessful()
    fun endTransaction()

    fun prepare(query: String): SqliteStmt
    fun prepare(query: String, hint: Int): SqliteStmt

    companion object {
        // the purpose of these is to help use the crippled Android Sqlite API efficiently
        val HINT_DEFAULT = 0
        val HINT_SINGLE_LONG = 1
        val HINT_SINGLE_TEXT = 2
    }
}

expect fun createSqliteHandle(path: String, openFlags: Int): SqliteHandle
