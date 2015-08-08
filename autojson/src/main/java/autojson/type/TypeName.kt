package autojson.type

import java.util.*

/**
 * Created by omochi on 15/08/08.
 */

class TypeName (
        val name: String,
        val params: Map<String, TypeName> = emptyMap()
) {
    override fun hashCode(): Int {
        var hash = 1
        hash = (37 * hash) + name.hashCode()
        hash = (37 * hash) + params.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TypeName) { return false }
        return name == other.name &&
                params == other.params
    }

    override fun toString(): String {
        val paramsStr = if (params.size() > 0) {
            params.map { "${it.key}=${it.value}" }.joinToString(", ", prefix = "<", postfix = ">")
        } else {
            ""
        }
        return "$name$paramsStr"
    }
}