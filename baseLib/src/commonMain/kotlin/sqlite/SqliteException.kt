package tsl.baseLib.sqlite

class SqliteException(val code: Int, message: String) : RuntimeException(message)
