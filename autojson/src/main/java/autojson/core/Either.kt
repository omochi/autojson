package autojson.core

/**
 * Created by omochi on 15/07/28.
 */

class Either<L, R> constructor(
        suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
        val left: L?,
        suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
        val right: R?
){
    suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
    fun component1(): L? = left
    suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
    fun component2(): R? = right

    suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
    inline fun <E> ifLeft(
            then: (value: L)-> E
    ): E? {
        if (left != null) {
            return then(left)
        }
        return null
    }

    suppress("BASE_WITH_NULLABLE_UPPER_BOUND")
    inline fun <E> ifRight(
            then: (value: R)-> E
    ): E? {
        if (right != null) {
            return then(right)
        }
        return null
    }

    inline fun <E> switch(
            caseLeft: (value: L)-> E,
            caseRight: (value: R)-> E
    ): E {
        ifLeft {
            return caseLeft(it)
        }
        ifRight {
            return caseRight(it)
        }
        throw Error("never reach here")
    }

    inline fun <T: Any> mapLeft (
            mapper: (value: L)-> T
    ): Either<T, R> {
        switch(
                { return Either.left(mapper(it)) },
                { return Either.right(it) }
        )
    }
    inline fun <T: Any> mapRight (
            mapper: (value: R)-> T
    ): Either<L, T> {
        switch(
                { return Either.left(it) },
                { return Either.right(mapper(it)) }
        )
    }

    inline fun toRight(
            mapper: (value: L)-> R
    ): R {
        switch(
                { return mapper(it) },
                { return it }
        )
    }
    inline fun toLeft(
            mapper: (value: R)-> L
    ): L {
        switch(
                { return it },
                { return mapper(it) }
        )
    }

    companion object {
        fun <L, R> left(value: L): Either<L, R> {
            return Either(value, null)
        }
        fun <L, R> right(value: R): Either<L, R> {
            return Either(null, value)
        }
    }
}

