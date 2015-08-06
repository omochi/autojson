package autojson.node

import autojson.DebugWriter

/**
 * Created by omochi on 15/07/30.
 */

class ClassDefNode (
        val let: List<String>,
        val name: String?,
        val fields: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("let=$let")
        w.writeLine("name=$name")
        w.indent("fields={", "}") {
            for ((name, field) in fields) {
                w.writeLine("$name=", false)
                w.writeObject(field)
            }
        }
    }
}