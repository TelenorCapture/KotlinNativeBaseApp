package tsl.baseLib.sqlite

object SqliteCode {
    val OK = 0
    val ERROR = 1   /* SQL error or missing database */
    val INTERNAL = 2   /* Internal logic error in SQLite */
    val PERM = 3   /* Access permission denied */
    val ABORT = 4   /* Callback routine requested an abort */
    val BUSY = 5   /* The database file is locked */
    val LOCKED = 6   /* A table in the database is locked */
    val NOMEM = 7   /* A malloc() failed */
    val READONLY = 8   /* Attempt to write a readonly database */
    val INTERRUPT = 9   /* Operation terminated by sqlite3_interrupt()*/
    val IOERR = 10   /* Some kind of disk I/O error occurred */
    val CORRUPT = 11   /* The database disk image is malformed */
    val NOTFOUND = 12   /* Unknown opcode in sqlite3_file_control() */
    val FULL = 13   /* Insertion failed because database is full */
    val CANTOPEN = 14   /* Unable to open the database file */
    val PROTOCOL = 15   /* Database lock protocol error */
    val EMPTY = 16   /* Database is empty */
    val SCHEMA = 17   /* The database schema changed */
    val TOOBIG = 18   /* String or BLOB exceeds size limit */
    val CONSTRAINT = 19   /* Abort due to constraint violation */
    val MISMATCH = 20   /* Data type mismatch */
    val MISUSE = 21   /* Library used incorrectly */
    val NOLFS = 22   /* Uses OS features not supported on host */
    val AUTH = 23   /* Authorization denied */
    val FORMAT = 24   /* Auxiliary database format error */
    val RANGE = 25   /* 2nd parameter to sqlite3_bind out of range */
    val NOTADB = 26   /* File opened that is not a database file */
    val NOTICE = 27   /* Notifications from sqlite3_log() */
    val WARNING = 28   /* Warnings from sqlite3_log() */
    val ROW = 100  /* sqlite3_step() has another row ready */
    val DONE = 101  /* sqlite3_step() has finished executing */
}
