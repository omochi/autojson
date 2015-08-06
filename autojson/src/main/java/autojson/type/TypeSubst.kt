package autojson.type

import autojson.DebugWritable
import autojson.DebugWriter

/**
 * Created by omochi on 15/08/03.
 */

class TypeSubst (
        val left: Type,
        val right: Type
): DebugWritable {
    override fun debugWrite(writer: DebugWriter) {
        writer.indent("TypeSubst(", ")") {
            writer.writeLine("left=", false)
            writer.writeObject(left)
            writer.writeLine("right=", false)
            writer.writeObject(right)
        }
    }
}