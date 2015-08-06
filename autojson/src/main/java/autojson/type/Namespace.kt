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
        val key: String,
        val table: MutableMap<String, Type> = LinkedHashMap()
): DebugWritable {
    val keys: List<String>
        get() {
            val parentKeys = parent?.keys ?: emptyList()
            return parentKeys + listOf(key)
        }

    fun resolve(name: String): TupleNamespaceType? {
        val type = table[name]
        if (type != null) {
            return TupleNamespaceType(this, type)
        }
        return parent?.resolve(name)
    }

    fun addEntry(name: String, type: Type): Either<Exception, Unit> {
        if (name in table) {
            return Either.left(Exception(
                    "namespace entry conflict: name=$name"
            ))
        }
        table[name] = type
        return Either.right(Unit)
    }
    fun setEntry(name: String, type: Type) {
//        println("namespace setEntry: namespace=$this, name=$name, type=${type.javaClass}")
        table[name] = type
    }

    override fun toString(): String {
        val parts = mapOf<String, String>(
                "keys" to keys.toString(),
                "entry num" to table.size().toString()
        )
        return "Namespace(" +
                parts.map { "${it.key}=${it.value}" }.joinToString(", ") +
                ")"
    }

    override fun debugWrite(writer: DebugWriter) {
        writer.indent("Namespace(", ")") {
            writer.indent("table={", "}") {
                for ((name, type) in table) {
                    writer.writeLine("$name=", false)
                    writer.writeObject(type)
                }
            }
        }
    }

}