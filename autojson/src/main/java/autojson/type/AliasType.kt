package autojson.type

import autojson.DebugWriter
import autojson.node.RefNode

/**
 * Created by omochi on 15/08/08.
 */

class AliasType (
        val ref: RefType
): Type() {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("ref=", false)
        w.writeObject(ref)
    }
}