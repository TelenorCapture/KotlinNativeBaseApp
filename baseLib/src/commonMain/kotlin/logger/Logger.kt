package tsl.baseLib.logger

enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}


internal expect fun outputMessage(level: LogLevel, msg: String)
internal expect fun reportException(e: Throwable)
internal expect fun getStackTrace(e: Throwable): String

object Logger {
    fun debug(msg: String) {
        outputMessage(LogLevel.DEBUG, msg)
    }

    fun info(msg: String) {
        outputMessage(LogLevel.INFO, msg)
    }

    fun warning(msg: String) {
        outputMessage(LogLevel.WARNING, msg)
    }

    fun error(msg: String) {
        outputMessage(LogLevel.ERROR, msg)
    }

    fun exception(e: Throwable, msg: String) {
        outputMessage(
            LogLevel.ERROR,
            msg + "\nMessage: " + e.message + "\nStacktrace: " + getStackTrace(e)
        )
        reportException(e)
    }
}
