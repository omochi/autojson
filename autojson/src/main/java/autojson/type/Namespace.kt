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
        private val table: MutableMap<TypeName, Type> = LinkedHashMap()
): DebugWritable {
    val debugKeys: List<String>
        get() {
            val parentKeys = parent?.debugKeys ?: emptyList()

            val selfKey = key.toString() + table.keySet().joinToString(", ", "{", "}")

            return parentKeys + listOf(selfKey)
        }

    fun resolve(name: TypeName): TupleNamespaceType? {
        val type = table[name]
        if (type != null) {
            return TupleNamespaceType(this, type)
        }
        return parent?.resolve(name)
    }

    fun getEntry(name: TypeName): Type? {
        return table[name]
    }
    fun getEntryNames(): List<TypeName> {
        return table.keySet().toList()
    }

    fun addEntry(name: TypeName, type: Type): Either<Exception, Unit> {
        if (name in table) {
            return Either.left(Exception(
                    "namespace entry conflict: name=$name"
            ))
        }
        table[name] = type
        return Either.right(Unit)
    }
    fun setEntry(name: TypeName, type: Type) {
//        println("namespace setEntry: namespace=$this, name=$name, type=${type.javaClass}")
        table[name] = type
    }

    override fun toString(): String {
        val parts = mapOf<String, String>(
                "keys" to debugKeys.toString()
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