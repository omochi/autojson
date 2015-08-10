package autojson.type

import java.util.*

/**
 * Created by omochi on 15/08/08.
 */

class TypeName (
        val name: String,
        val anonymous: Boolean,
        val params: List<TypeName> = emptyList()
) {
    constructor(other: TypeName):
        this(other.name, other.anonymous, other.params)
    {
    }

    override fun hashCode(): Int {
        var hash = 1
        if (anonymous) {
            hash = (37 * hash) + 1
            hash = (37 * hash) + System.identityHashCode(this)
            return hash
        } else {
            hash = (37 * hash) + 0
        }
        hash = (37 * hash) + name.hashCode()
        hash = (37 * hash) + params.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TypeName) { return false }
        if (anonymous) {
            return this === other
        }
        return name == other.name &&
                params == other.params
    }

    override fun toString(): String {
        val paramsStr = if (params.size() > 0) {
            params.map { "${it}" }.joinToString(", ", prefix = "<", postfix = ">")
        } else {
            ""
        }
        val baseStr = "$name$paramsStr"
        if (anonymous) {
            return "($baseStr@${System.identityHashCode(this)})"
        } else {
            return baseStr
        }
    }

}