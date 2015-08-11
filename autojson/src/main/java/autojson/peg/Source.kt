package autojson.peg

/**
 * Created by omochi on 15/08/11.
 */

class Source(
        val string: String,
        val pos: Int,
        val line: Int,
        val column: Int
) {
    constructor(string: String): this(
            string,
            0,
            0,
            0
    ){}

    override fun hashCode(): Int {
        var hash = 1
        hash = (hash * 37) + System.identityHashCode(string)
        hash = (hash * 37) + pos.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Source) {
            return false
        }
        return string === other.string &&
            pos == other.pos
    }

    override fun toString(): String {
        return "Source(line=$line, column=$column, pos=$pos)"
    }

    fun asSequence(): CharSequence {
        return string.subSequence(pos, string.length())
    }

    fun read(): TupleSourceChar? {
        val s1 = readChar()
        when (s1) {
            null -> { return null }
            '\r' -> {
                val next = Source(string, pos + 1, line, column + 1)
                val s2 = next.readChar()
                when (s2) {
                    null -> {
                        return TupleSourceChar(
                                Source(string, pos + 1, line + 1, 0),
                                s1
                        )
                    }
                    '\n' -> {
                        return TupleSourceChar(
                                Source(string, pos + 1, line, column + 1),
                                s1
                        )
                    }
                    else -> {
                        return TupleSourceChar(
                                Source(string, pos + 1, line + 1, 0),
                                s1
                        )
                    }
                }
            }
            '\n' -> {
                return TupleSourceChar(
                        Source(string, pos + 1, line + 1, 0),
                        s1
                )
            }
            else -> {
                return TupleSourceChar(
                        Source(string, pos + 1, line, column + 1),
                        s1
                )
            }
        }
    }

    fun read(len: Int): TupleSourceString? {
        val builder = StringBuilder()
        var next = this
        while (builder.length() < len) {
            val ret = next.read() ?: return null
            builder.append(ret.char)
            next = ret.source
        }
        return TupleSourceString(
                next,
                builder.toString()
        )
    }

    private fun readChar(): Char? {
        if (pos >= string.length()) {
            return null
        }
        return string[pos]
    }
}

data class TupleSourceChar(
        val source: Source,
        val char: Char
)

data class TupleSourceString(
        val source: Source,
        val string: String
)