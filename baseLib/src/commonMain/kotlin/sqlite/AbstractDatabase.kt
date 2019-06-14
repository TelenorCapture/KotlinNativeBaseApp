package tsl.baseLib.sqlite

import kotlinx.io.core.use
import tsl.baseLib.FileSystem
import tsl.baseLib.Logger

abstract class AbstractDatabase protected constructor(
    private val dbPath: String,
    private val dbFlags: Int,
    private val getCurrentExecutorName: () -> String
) {
    private val databaseExecutorName: String = getCurrentExecutorName()

    private var db: SqliteHandle? = null
    lateinit var sqliteVersion: SqliteVersion
        protected set

    protected abstract val currentVersion: Int

    val isClosed: Boolean
        get() = db == null

    val handle: SqliteHandle
        get() {
            throwIfNotDbThread()
            return db!!
        }

    var userVersion: Int
        get() {
            throwIfNotDbThread()
            db!!.prepare("PRAGMA user_version").use { stmt ->
                if (!stmt.step()) {
                    throw RuntimeException("unable to get database version number")
                }
                return stmt.getInt(0)
            }
        }
        set(newVersion) {
            throwIfNotDbThread()
            db!!.prepare("PRAGMA user_version = $newVersion").use { stmt -> stmt.step() }
        }

    class UnknownOldVersionException(val version: Int) : Exception("Unknown old version: $version")

    protected abstract fun migrateDatabase(oldVersion: Int, targetVersion: Int)

    protected fun onBeforeClose() {}
    protected fun onAfterOpen() {}

    init {
        Logger.debug("Created database instance on executor: $databaseExecutorName")
    }

    fun open() {
        throwIfNotDbThread()
        if (db != null) {
            throw RuntimeException("database already open")
        }
        db = createSqliteHandle(dbPath, dbFlags)
        sqliteVersion = SqliteVersion(db!!)
        onAfterOpen()
    }

    fun close() {
        throwIfNotDbThread()
        if (db == null) {
            return
        }
        onBeforeClose()
        db!!.close()
        db = null
    }

    fun <T> initialize(initFunc: () -> T): T {
        ensureDirExists()
        open()
        try {
            return initFunc()
        } catch (e: Exception) {
            Logger.exception(e, "error initializing database; deleting and retrying")
            close()
            deleteDbFile()
            open()
            try {
                return initFunc()
            } catch (e2: Exception) {
                close()
                throw RuntimeException("error initializing database: $dbPath", e2)
            }

        }
    }

    private fun ensureDirExists() {
        if (dbPath != ":memory:") {
            if (!FileSystem.isDirectory(dbPath)) {
                if (!FileSystem.makeDirs(dbPath)) {
                    Logger.error("could not create database folder: $dbPath")
                }
            }
        }
    }

    private fun deleteDbFile() {
        if (dbPath != ":memory:") {
            if (!FileSystem.delete(dbPath) && FileSystem.exists(dbPath)) {
                Logger.error("error deleting database file: $dbPath")
            }
        }
    }

    fun setSqliteVersionForTestPurposes(version: SqliteVersion) {
        sqliteVersion = version
    }

    fun throwIfNotDbThread() {
        val currentName = getCurrentExecutorName()
        if (currentName !== databaseExecutorName) {
            throw RuntimeException(
                "don't do database operations outside the DB thread. current thread is: $currentName"
            )
        }
    }

    fun migrateToCurrentVersion() {
        migrateToVersion(currentVersion)
    }

    fun migrateToVersion(targetVersion: Int) {
        throwIfNotDbThread()

        val oldVersion = userVersion

        if (targetVersion != oldVersion) {
            migrateDatabase(oldVersion, targetVersion)
            userVersion = targetVersion
        }
    }
}
