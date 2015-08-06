package autojson.node

import autojson.DebugWriter
import autojson.node.Node

/**
 * Created by omochi on 15/07/28.
 */

class RefNode(
        val name: String,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("name=$name")
    }
}