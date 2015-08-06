package autojson.type

import autojson.DebugWriter

/**
 * Created by omochi on 15/08/05.
 */

class ArrowType(
        val namespace: Namespace,
        val params: Map<String, Type>,
        val body: Type
): Type() {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("namespace=", false)
        w.indent("params={", "}") {
            for ((name, param) in params) {
                w.writeLine("$name=", false)
                w.writeObject(param)
            }
        }
        w.writeLine("body=", false)
        w.writeObject(body)
    }
}