package autojson.type

import autojson.DebugWriter

/**
 * Created by omochi on 15/08/02.
 */

class RefType (
        val namespace: Namespace,
        val name: String
): Type() {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("namespace=$namespace")
        w.writeLine("name=$name")
    }
}