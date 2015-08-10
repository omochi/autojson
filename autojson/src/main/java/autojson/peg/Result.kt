package autojson.peg

/**
 * Created by omochi on 15/08/11.
 */

abstract class Result<out T> {
    inline fun <U> map(mapper: (Ok<T>)-> Ok<U>): Result<U> {
        when (this) {
            is Ok -> {
                return Ok(mapper(this))
            }
            is Error -> {
                return Error(this)
            }
            else -> { throw Exception("never reach") }
        }
    }
    inline fun <U> flatMap(mapper: (Ok<T>)-> Result<U>): Result<U> {
        when (this) {
            is Ok -> {
                return mapper(this)
            }
            is Error -> {
                return Error(this)
            }
            else -> { throw Exception("never reach") }
        }
    }
}

class Ok<out T>(
        val sourceBegin: Source,
        val sourceEnd: Source,
        val value: T
): Result<T>() {

    constructor(other: Ok<T>): this(
            other.sourceBegin,
            other.sourceEnd,
            other.value
    ){

    }

    override fun toString(): String {
        return "Ok(source=$sourceBegin..<$sourceEnd, value=$value)"
    }

    fun <U> mapValue(mapper: (T)-> U): Ok<U> {
        return Ok(sourceBegin, sourceEnd, mapper(value))
    }
}

class Error (
        val source: Source,
        val message: String
): Result<Nothing>() {

    constructor(other: Error): this(
            other.source,
            other.message
    ){

    }

    override fun toString(): String {
        return "Error(source=$source, message=$message)"
    }
}