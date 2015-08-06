package autojson.json

import autojson.core.Either
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringReader
import java.lang
import java.util.*

/**
 * Created by omochi on 15/07/28.
 */

class Json {
    enum class ValueType {
        Null,
        Boolean,
        Int,
        Long,
        Float,
        Double,
        String,
        List,
        Map
    }

    val value: Any?

    private class PrimaryConstructorTag {}
    suppress("UNUSED_PARAMETER")
    private constructor(
            value: Any?,
            tag: Json.PrimaryConstructorTag
    ) {
        this.value = value
    }

    constructor(value: Nothing?): this(value, PrimaryConstructorTag()) {}
    constructor(value: Boolean): this(value, PrimaryConstructorTag()) {}
    constructor(value: Int): this(value, PrimaryConstructorTag()) {}
    constructor(value: Long): this(value, PrimaryConstructorTag()) {}
    constructor(value: Float): this(value, PrimaryConstructorTag()) {}
    constructor(value: Double): this(value, PrimaryConstructorTag()) {}
    constructor(value: String): this(value, PrimaryConstructorTag()) {}
    constructor(value: Iterable<Json>):
        this(value.map { it.value }, PrimaryConstructorTag()){}
    constructor(value: Map<String, Json>):
        this(value.mapValues { it.value.value }, PrimaryConstructorTag()) {}

    val valueType: ValueType
        get() {
            when (value) {
                null -> return ValueType.Null
                is Boolean -> return ValueType.Boolean
                is Int -> return ValueType.Int
                is Long -> return ValueType.Long
                is Float -> return ValueType.Float
                is Double -> return ValueType.Double
                is String -> return ValueType.String
                is List<*> -> return ValueType.List
                is Map<*, *> -> return ValueType.Map
                else -> throw Error("invalid type value: ${value.javaClass}")
            }
        }

    val asNull: Nothing? = null
    val asBoolean: Boolean?
        get() {
            if (value is Boolean) { return value }
            return null
        }
    val asInt: Int?
        get() {
            val number = asNumberRaw?.toInt()
            if (number != null) { return number }
            return null
        }
    val asLong: Long?
        get() {
            val number = asNumberRaw?.toLong()
            if (number != null) { return number }
            return null
        }
    val asFloat: Float?
        get() {
            val number = asNumberRaw?.toFloat()
            if (number != null) { return number }
            return null
        }
    val asDouble: Double?
        get() {
            val number = asNumberRaw?.toDouble()
            if (number != null) { return number }
            return null
        }
    val asString: String?
        get() {
            if (value is String) { return value }
            return null
        }
    val asList: List<Json>?
        get() {
            if (value is List<*>) {
                return value.map { Json(it, PrimaryConstructorTag()) }
            }
            return null
        }
    val asMap: Map<String, Json>?
        get() {
            val mapValue = value as? Map<*, *>
            if (mapValue != null) {
                val map = LinkedHashMap<String, Json>()
                for (entry in mapValue.entrySet()) {
                    val key = entry.key
                    if (key !is String) { continue }
                    map[key] = Json(entry.value, PrimaryConstructorTag())
                }
                return map
            }
            return null
        }

    fun get(index: Int): Json {
        return Json((value as? List<*>)?.elementAtOrNull(index),
                PrimaryConstructorTag())
    }

    fun get(key: String): Json {
        return Json((value as? Map<*, *>)?.get(key),
                PrimaryConstructorTag())
    }

    private val asNumberRaw: Number?
        get() {
            if (value is Int) { return value }
            if (value is Long) { return value }
            if (value is Float) { return value }
            if (value is Double) { return value }
            return null
        }

    override fun toString(): String {
        return "Json(${value.toString()})"
    }

    companion object {
        fun parse(string: String): Either<Exception, Json> {
            val mapper = ObjectMapper()

            val value = try {
                mapper.readValue(string, javaClass<Any>())
            } catch(e: Exception) {
                return Either.left(e)
            }
            return Either.right(Json(value, PrimaryConstructorTag()))
        }
    }
}

fun jsonArrayOf(): Json {
    return Json(emptyList())
}
fun jsonArrayOf(vararg values: Json): Json {
    return Json(listOf(*values))
}
fun jsonObjectOf(): Json {
    return Json(emptyMap())
}
fun jsonObjectOf(vararg keyValues: Pair<String, Json>): Json {
    return Json(mapOf(*keyValues))
}

fun Nothing?.toJson(): Json = Json(this)
fun Boolean.toJson(): Json = Json(this)
fun Int.toJson(): Json = Json(this)
fun Long.toJson(): Json = Json(this)
fun Float.toJson(): Json = Json(this)
fun Double.toJson(): Json = Json(this)
fun String.toJson(): Json = Json(this)
fun Iterable<Json>.toJson(): Json = Json(this)
fun Map<String, Json>.toJson(): Json = Json(this)