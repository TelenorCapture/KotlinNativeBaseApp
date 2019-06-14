package tsl.baseLib.sqlite

import kotlinx.io.core.use

fun SqliteHandle.execute(query: String, vararg params: Any?) {
    this.prepare(query).use { stmt ->
        var col = 1
        for (obj in params) {
            when (obj) {
                null -> stmt.bindNull(col)
                is Int -> stmt.bindInt(col, obj)
                is Long -> stmt.bindLong(col, obj)
                is Float -> stmt.bindFloat(col, obj)
                is Double -> stmt.bindDouble(col, obj)
                is String -> stmt.bindText(col, obj)
                is ByteArray -> stmt.bindBlob(col, obj)
                else -> throw RuntimeException("unsupported param type")
            }
            ++col
        }
        stmt.step()
    }
}

fun SqliteHandle.withTransaction(func: () -> Unit) {
    this.beginTransaction()
    try {
        func()
        this.setTransactionSuccessful()
    } finally {
        this.endTransaction()
    }
}

fun SqliteStmt.expectStep() {
    if (!this.step()) {
        throw RuntimeException("step() returned false")
    }
}
