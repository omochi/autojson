package autojson.node

import autojson.DebugWriter

/**
 * Created by omochi on 15/07/30.
 */

class ApplyNode(
        val let: List<String>,
        val name: String?,
        val target: Node,
        val params: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("let=$let")
        w.writeLine("name=$name")
        w.writeLine("target=", false)
        w.writeObject(target)
        w.indent("params={", "}") {
            for ((name, param) in params) {
                w.writeLine("$name=", false)
                w.writeObject(param)
            }
        }
    }
}