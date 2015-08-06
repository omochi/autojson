package autojson.core

/**
 * Created by omochi on 15/07/29.
 */

interface Box<out T> {
    val value: T
}

interface MutableBox<T> : Box<T> {
    override var value: T
}

class MutableBoxImpl<T> : MutableBox<T> {
    override var value: T

    constructor(value: T) {
        this.value = value
    }
}

fun <T> boxOf(value: T): Box<T> {
    return mutableBoxOf(value)
}
fun <T> mutableBoxOf(value: T): MutableBox<T> {
    return MutableBoxImpl(value)
}