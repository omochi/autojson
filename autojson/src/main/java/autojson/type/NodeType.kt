package autojson.type

import autojson.DebugWriter
import autojson.node.Node

/**
 * Created by omochi on 15/07/31.
 */

class NodeType (
        val node: Node
): Type() {
    override fun toString(): String {
        return "NodeType(node pos=${node.pos})"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NodeType) {
            return false
        }
        return node == other.node
    }
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("node=${node.javaClass.getSimpleName()}")
        w.writeLine("pos=${node.pos}")
    }

    override fun applySubsts(subst: NameSubstTable): NodeType {
        return this
    }
}