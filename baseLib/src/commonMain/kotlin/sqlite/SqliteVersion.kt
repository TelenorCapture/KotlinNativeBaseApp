package tsl.baseLib.sqlite

import kotlinx.io.core.use
import tsl.baseLib.logger.Logger

class SqliteVersion : Comparable<SqliteVersion> {

    private val majorVersion: Int
    private val minorVersion: Int
    private val patchVersion: Int
    private val otherVersion: Int

    constructor(db: SqliteHandle) {
        var majorVersion = 0
        var minorVersion = 0
        var patchVersion = 0
        var otherVersion = 0

        var sqliteVersionString = ""
        try {
            db.prepare("select sqlite_version() AS sqlite_version").use { versionQueryStmt ->
                versionQueryStmt.reset()
                versionQueryStmt.step()
                sqliteVersionString = versionQueryStmt.getText(0) ?: ""

                // An sqlite version number has two, three or four parts.
                // https://www.sqlite.org/versionnumbers.html
                val versionComponents =
                    sqliteVersionString.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (versionComponents.size >= 2) {
                    majorVersion = versionComponents[0].toInt()
                    minorVersion = versionComponents[1].toInt()
                }
                if (versionComponents.size >= 3) {
                    patchVersion = versionComponents[2].toInt()
                }
                if (versionComponents.size >= 4) {
                    otherVersion = versionComponents[3].toInt()
                }
            }
        } catch (e: NumberFormatException) {
            Logger.exception(
                e, "Failed to parse sql version string: " + sqliteVersionString +
                        " Assuming version 3.8.4"
            )
            majorVersion = 3
            minorVersion = 8
            patchVersion = 4
            otherVersion = 0
        } catch (e: Exception) {
            Logger.exception(e, "Failed to retrieve Sqlite version string; Assuming version 3.8.4")
            majorVersion = 3
            minorVersion = 8
            patchVersion = 4
            otherVersion = 0
        }

        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
        this.patchVersion = patchVersion
        this.otherVersion = otherVersion
    }

    constructor(majorVersion: Int, minorVersion: Int, patchVersion: Int, otherVersion: Int) {
        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
        this.patchVersion = patchVersion
        this.otherVersion = otherVersion
    }

    fun hasPartialIndexSupport(): Boolean {
        return this >= FIRST_VERSION_WITH_PARTIAL_INDEX_SUPPORT
    }

    fun hasCteSupport(): Boolean {
        return this >= FIRST_VERSION_WITH_CTE_SUPPORT
    }

    override fun compareTo(other: SqliteVersion): Int {
        // https://www.sqlite.org/changes.html
        return ((this.majorVersion - other.majorVersion) * 65536
                + (this.minorVersion - other.minorVersion) * 1024
                + (this.patchVersion - other.patchVersion) * 32
                + (this.otherVersion - other.otherVersion))
    }

    companion object {
        val VERSION_WITHOUT_CTE_SUPPORT = SqliteVersion(3, 5, 0, 0)
        val FIRST_VERSION_WITH_CTE_SUPPORT = SqliteVersion(3, 8, 3, 0)
        val FIRST_VERSION_WITH_PARTIAL_INDEX_SUPPORT = SqliteVersion(3, 8, 0, 0)
    }
}
