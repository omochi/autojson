package autojson.node

import java.io.File

/**
 * Created by omochi on 15/08/02.
 */

class NodePos(
        val file: File,
        val keys: List<String>
) {
    override fun hashCode(): Int {
        var hash = 1
        hash = (hash * 37) + file.hashCode()
        hash = (hash * 37) + keys.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NodePos) { return false }
        return file.getAbsolutePath() == other.file.getAbsolutePath() &&
                keys == other.keys
    }

    override fun toString(): String {
        if (keys.size() == 0) {
            return file.getAbsolutePath()
        }
        val strs = listOf(file.getName()) + keys
        return strs.toString()
    }

    fun plus(key: String): NodePos {
        return NodePos(file, keys + listOf(key))
    }
}