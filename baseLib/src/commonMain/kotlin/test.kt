package tsl.baseLib

import kotlinx.io.core.use
import kotlinx.serialization.*
import kotlinx.serialization.json.Json.Companion.stringify
import tsl.baseLib.logger.Logger
import tsl.baseLib.sqlite.AbstractDatabase
import tsl.baseLib.sqlite.execute
import tsl.baseLib.sqlite.expectStep
import tsl.baseLib.sqlite.withTransaction

@Serializable
data class Data(val a: Int, val b: String = "42")

fun generateJson(): String {
    @Suppress("EXPERIMENTAL_API_USAGE")
    return stringify(Data.serializer(), Data(42))
}

class TestDatabase(
    dbPath: String,
    dbFlags: Int
) : AbstractDatabase(dbPath, dbFlags, {"foo"}) {

    override val currentVersion: Int
        get() = 1

    override fun migrateDatabase(oldVersion: Int, targetVersion: Int) {
        if (oldVersion != 0) {
            throw UnknownOldVersionException(oldVersion)
        }
        if (targetVersion != currentVersion) {
            throw RuntimeException("bad version")
        }
        handle.execute("CREATE TABLE test (key INTEGER PRIMARY KEY, data INTEGER)")
    }
}

fun useTestDatabase() {
    val db = TestDatabase(":memory:", 0)
    db.open()
    try {
        val handle = db.handle
        handle.withTransaction {
            db.migrateToCurrentVersion()
            handle.execute("INSERT INTO test (key, data) VALUES (?, ?)", 1, 2)
            handle.prepare("SELECT data FROM test WHERE key = ?").use { stmt ->
                stmt.bindInt(1, 1)
                stmt.expectStep()
                Logger.debug("Found value: " + stmt.getInt(0))
            }
        }
    } finally {
        db.close()
    }
}
