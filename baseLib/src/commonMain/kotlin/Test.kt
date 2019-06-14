package tsl.baseLib

import kotlinx.io.core.use
import kotlinx.serialization.*
import kotlinx.serialization.json.Json.Companion.stringify
import tsl.baseLib.sqlite.*

@Serializable
data class TestData(val a: Int, val b: String = "42")

fun generateJson(): String {
    @Suppress("EXPERIMENTAL_API_USAGE")
    return stringify(TestData.serializer(), TestData(42))
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
    db.withOpen { handle ->
        handle.withTransaction {
            db.migrateToCurrentVersion()
            handle.execute("INSERT INTO test (key, data) VALUES (?, ?)", 1, 2)
            handle.prepare("SELECT data FROM test WHERE key = ?").use { stmt ->
                stmt.bindInt(1, 1)
                stmt.expectStep()
                Logger.debug("Found value: " + stmt.getInt(0))
            }
        }
    }
}
