package autojson.type

import autojson.DebugWriter

/**
 * Created by omochi on 15/08/02.
 */

class ClassType(
        val name: String,
        val params: Map<String, Type>,
        val namespace: Namespace,
        val fields: Map<String, Type>
): Type() {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("name=$name")

        w.indent("params={", "}") {
            for ((name, param) in params) {
                w.writeLine("$name=", false)
                w.writeObject(param)
            }
        }

//        w.writeLine("namespace=", false)
//        w.writeObject(namespace)

        w.writeLine("namespace=$namespace")

        w.indent("fields={", "}") {
            for ((name, field) in fields) {
                w.writeLine("$name=", false)
                w.writeObject(field)
            }
        }
    }

}