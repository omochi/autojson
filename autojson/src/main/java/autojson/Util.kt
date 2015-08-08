package autojson

import autojson.type.TypeName

/**
 * Created by omochi on 15/08/03.
 */

fun getClassNameFromFieldName(name: String): String? {
    if (name.length() == 0) { return null }
    val className = name.substring(0, 1).toUpperCase() + name.substring(1)
    return className
}