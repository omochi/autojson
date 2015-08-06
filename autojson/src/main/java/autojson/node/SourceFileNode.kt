package autojson.node

import autojson.DebugWriter

/**
 * Created by omochi on 15/07/30.
 */

class SourceFileNode (
        val types: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.indent("types={", "}") {
            for ((name, type) in types) {
                w.writeLine("$name=", false)
                w.writeObject(type)
            }
        }
    }
}