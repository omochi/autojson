package autojson.peg

import java.util.*
import kotlin.text.Regex
import kotlin.text.RegexOption

/**
 * Created by omochi on 15/08/11.
 */

abstract class Parser<out T> {
    private val thiz: Parser<T> = this

    private val resultCacheMap: MutableMap<Source, Result<T>> = HashMap()

    fun parse(source: Source): Result<T> {
        val cached  = resultCacheMap[source]
        if (cached != null) {
            return cached
        }
        val ret = doParse(source)
        resultCacheMap[source] = ret
        return ret
    }

    protected abstract fun doParse(source: Source): Result<T>

    fun <U> mapResult(mapper: (Ok<T>)-> Ok<U>): Parser<U> {
        return object : Parser<U>() {
            override fun toString(): String {
                return "$thiz"
            }
            override fun doParse(source: Source): Result<U> {
                return thiz.parse(source).map {
                    mapper(it)
                }
            }
        }
    }

    fun <U> map(mapper: (T)-> U): Parser<U> {
        return mapResult {
            it.mapValue (mapper)
        }
    }

    fun wrap(): Parser<List<T>> {
        return map { listOf(it) }
    }
}

fun <T> Parser<Iterable<Iterable<T>>>.flatten(): Parser<List<T>> {
    return map { it.flatten() }
}

fun literal(literal: String): Parser<String> {
    return object : Parser<String>() {
        override fun toString(): String {
            return "literal($literal)"
        }
        override fun doParse(source: Source): Result<String> {
            source.read(literal.length()).let {
                if (it == null) {
                    return Error(source, "reach end: $this")
                }
                val (newSource, string) = it
                if (string != literal) {
                    return Error(source, "not matched: $this")
                }
                return Ok(source, newSource, string)
            }
        }
    }
}

fun <T> empty(value: T): Parser<T> {
    return object : Parser<T>() {
        override fun toString(): String {
            return "empty()"
        }
        override fun doParse(source: Source): Result<T> {
            return Ok(source, source, value)
        }
    }
}

fun dot(): Parser<Char> {
    return object : Parser<Char>() {
        override fun toString(): String {
            return "dot()"
        }
        override fun doParse(source: Source): Result<Char> {
            source.read().let {
                if (it == null) {
                    return Error(source, "reach end")
                }
                return Ok(source, it.source, it.char)
            }
        }
    }
}

fun regex(pattern: String, options: Set<RegexOption> = emptySet()): Parser<String> {
    val regex = Regex("^$pattern", options)
    return object : Parser<String>() {
        override fun toString(): String {
            return "regex($regex)"
        }
        override fun doParse(source: Source): Result<String> {
            val matchRet = regex.match(source.asSequence()) ?:
                    return Error(source, "not matched: $this")
            if (matchRet.range.start != 0) {
                return Error(source, "not matched: $this")
            }
            val ret = source.read(matchRet.value.length())!!
            return Ok(source, ret.source, ret.string)
        }
    }
}

fun <T> seq(vararg parsers: Parser<T>): Parser<List<T>> {
    return object : Parser<List<T>>() {
        override fun toString(): String {
            return "seq(${parsers.toList()})"
        }
        override fun doParse(source: Source): Result<List<T>> {
            val ret = ArrayList<T>()
            var currentSource = source

            for (parser in parsers) {
                val parseRet = parser.parse(currentSource)
                when (parseRet) {
                    is Ok -> {
                        ret.add(parseRet.value)
                        currentSource = parseRet.sourceEnd
                    }
                    is Error -> {
                        return Error(parseRet)
                    }
                }
            }

            return Ok(source, currentSource, ret)
        }
    }
}

fun <T> flatSeq(vararg parsers: Parser<Iterable<T>>): Parser<List<T>> {
    return seq(*parsers).flatten()
}

fun <T> Parser<T>.div(right: Parser<T>): Parser<T> {
    val left = this
    return object : Parser<T>() {
        override fun toString(): String {
            return "$left / $right"
        }
        override fun doParse(source: Source): Result<T> {
            val ret1 = left.parse(source)
            if (ret1 is Ok) {
                return ret1
            }
            val ret2 = right.parse(source)
            if (ret2 is Ok) {
                return ret2
            }
            return Error(source, "not matched: $this")
        }
    }
}

fun <T> ref(capture: ()-> Parser<T>): Parser<T> {
    return object : Parser<T>() {
        override fun toString(): String {
            return "ref(...)"
        }
        override fun doParse(source: Source): Result<T> {
            return capture().parse(source)
        }
    }
}

class ParserRef<T>: Parser<T>() {
    var value: Parser<T>? = null
    override fun toString(): String {
        if (value != null) {
            return "ref(...)"
        } else {
            return "ref(null)"
        }
    }
    override fun doParse(source: Source): Result<T> {
        val value = this.value ?:
                throw Exception("ref is null")
        return value.parse(source)
    }
}

suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
fun <T> Parser<T>.opt(): Parser<T?> {
    return this / empty(null)
}

fun <T> Parser<T>.zeroOrMore(): Parser<List<T>> {
    var tail = empty(emptyList<T>())

    tail = flatSeq(
            wrap(),
            ref { tail }
    ) / empty(emptyList<T>())

    return tail
}

fun <T> Parser<T>.oneOrMore(): Parser<List<T>> {
    return flatSeq(
            wrap(),
            zeroOrMore()
    )
}

fun <T> andPred(parser: Parser<T>): Parser<Unit> {
    return object : Parser<Unit>() {
        override fun toString(): String {
            return "andPred($parser)"
        }
        override fun doParse(source: Source): Result<Unit> {
            val parseRet = parser.parse(source)
            when (parseRet) {
                is Ok -> {
                    return Ok(source, source, Unit)
                }
            }
            return Error(source, "not matched: $this")
        }
    }
}

fun <T> notPred(parser: Parser<T>): Parser<Unit> {
    return object : Parser<Unit>() {
        override fun toString(): String {
            return "notPred($parser)"
        }
        override fun doParse(source: Source): Result<Unit> {
            val parseRet = parser.parse(source)
            when (parseRet) {
                is Ok -> {
                    return Error(source, "not matched: $this")
                }
            }
            return Ok(source, source, Unit)
        }
    }
}