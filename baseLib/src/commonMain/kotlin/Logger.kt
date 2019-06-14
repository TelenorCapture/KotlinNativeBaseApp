package tsl.baseLib

enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

object Logger {
    fun debug(msg: String) {
        outputLogMessage(LogLevel.DEBUG, msg)
    }

    fun info(msg: String) {
        outputLogMessage(LogLevel.INFO, msg)
    }

    fun warning(msg: String) {
        outputLogMessage(LogLevel.WARNING, msg)
    }

    fun error(msg: String) {
        outputLogMessage(LogLevel.ERROR, msg)
    }

    fun exception(e: Throwable, msg: String) {
        outputLogMessage(
            LogLevel.ERROR,
            msg + "\nMessage: " + e.message + "\nStacktrace: " + getStackTrace(e)
        )
        reportException(e)
    }
}
