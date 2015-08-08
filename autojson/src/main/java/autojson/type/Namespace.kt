package autojson.type

import autojson.DebugWritable
import autojson.DebugWriter
import autojson.core.Either
import java.util.*

/**
 * Created by omochi on 15/08/02.
 */

class Namespace(
        val parent: Namespace?,
        val key: TypeName,
        private val mutableTable: MutableMap<TypeName, Type> = LinkedHashMap()
): DebugWritable {
    val table: Map<TypeName, Type> = mutableTable

    val level: Int
        get() {
            val p = parent
            if (p != null) {
                return p.level + 1
            } else {
                return 0
            }
        }

    val debugKeys: List<String>
        get() {
            val parentKeys = parent?.debugKeys ?: emptyList()

            val (builtIns, users) = mutableTable.entrySet().partition {
                val type = it.value
                if (type is ClassType) {
                    type.isBuiltIn
                } else {
                    false
                }
            }

            val typeStrs = ArrayList<String>()
            if (builtIns.size() > 0) {
                typeStrs.add("BuiltIns(${builtIns.size()})")
            }
            if (users.size() > 3) {
                typeStrs.add("...")
            }
            typeStrs.addAll(
                    users.takeLast(3).map { it.key.toString() }
            )
            val selfKey = key.toString() + typeStrs.joinToString(", ", "{", "}")

            val keys = parentKeys + listOf(selfKey)
            return keys
        }

    fun resolve(name: TypeName): TupleNamespaceType? {
        val type = mutableTable[name]
        if (type != null) {
            return TupleNamespaceType(this, type)
        }
        return parent?.resolve(name)
    }

    fun setEntry(name: TypeName, type: Type) {
        mutableTable[name] = type
    }

    override fun toString(): String {
        val parts = mapOf<String, String>(
                "level" to level.toString(),
                "keys" to debugKeys.toString()
        )
        return "Namespace@${System.identityHashCode(this)}(" +
                parts.map { "${it.key}=${it.value}" }.joinToString(", ") +
                ")"
    }

    override fun debugWrite(writer: DebugWriter) {
        writer.indent("Namespace@${System.identityHashCode(this)}(", ")") {
            writer.indent("keys=[", "]") {
                for ((index, key) in debugKeys.withIndex()) {
                    writer.writeLine("[$index]=$key")
                }
            }
            writer.indent("table={", "}") {
                for ((name, type) in mutableTable) {
                    writer.writeLine("$name=", false)
                    writer.writeObject(type)
                }
            }
        }
    }

}