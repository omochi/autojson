package autojson.type

import autojson.DebugWritable
import autojson.DebugWriter
import java.util.*

/**
 * Created by omochi on 15/08/02.
 */

class TypeSubstTable(
        val entries: List<TypeSubst>
): DebugWritable {

    constructor(): this(emptyList()) {
    }

    fun push(subst: TypeSubst): TypeSubstTable {
        // TODO
        return this
    }
    override fun debugWrite(writer: DebugWriter) {
        writer.indent("TypeSubstTable(", ")") {
            for(entry in entries) {
                writer.writeObject(entry)
            }
        }
    }
}