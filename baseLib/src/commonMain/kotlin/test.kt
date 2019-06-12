package tsl.baseLib

import kotlinx.serialization.*
import kotlinx.serialization.json.Json.Companion.stringify

@Serializable
data class Data(val a: Int, val b: String = "42")

fun generateJson(): String {
    @Suppress("EXPERIMENTAL_API_USAGE")
    return stringify(Data.serializer(), Data(42))
}

