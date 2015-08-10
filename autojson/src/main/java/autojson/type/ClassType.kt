package autojson.type

import autojson.DebugWriter
import java.util.*

/**
 * Created by omochi on 15/08/02.
 */

class ClassType(
        val name: TypeName,
        val isBuiltIn: Boolean,
        val params: MutableMap<String, Type>,
        val namespace: Namespace,
        val fields: Map<String, Type>
): Type() {

    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("name=$name")

        if (isBuiltIn) {
            w.writeLine("isBuiltIn=$isBuiltIn")
        }

        if (params.size() > 0) {
            w.indent("params={", "}") {
                for ((name, param) in params) {
                    w.writeLine("$name=", false)
                    w.writeObject(param)
                }
            }
        }

        w.writeLine("namespace=", false)
        w.writeObject(namespace)

        if (fields.size() > 0) {
            w.indent("fields={", "}") {
                for ((name, field) in fields) {
                    w.writeLine("$name=", false)
                    w.writeObject(field)
                }
            }
        }
    }

    val parentNamespaceNameTuple: TupleNamespaceTypeName =
            TupleNamespaceTypeName(namespace.parent!!, name)

    override fun applySubsts(substs: List<NamespaceEntrySubst>): ClassType {
        val (newParentNamespace, newClassName) = parentNamespaceNameTuple.applySubsts(substs)

        val newClassNamespace = Namespace(
                newParentNamespace,
                newClassName
        )
        val newSubsts = ArrayList(substs)
        for ((name, value) in namespace.table.entrySet()) {
            newSubsts.add(NamespaceEntrySubst(
                    TupleNamespaceTypeName(namespace, name),
                    TupleNamespaceTypeName(newClassNamespace, TypeName(name))
            ))
        }

        for ((name, value) in namespace.table.entrySet()) {
            val (appliedNamespace, appliedName) = TupleNamespaceTypeName(namespace, name).applySubsts(newSubsts)
            val appliedValue = value.applySubsts(newSubsts)

            appliedNamespace.setEntry(appliedName, appliedValue)
        }

        val newClassFields = fields.mapValues {
            it.value.applySubsts(newSubsts)
        }

        return ClassType(
                newClassName,
                isBuiltIn,
                LinkedHashMap(params),
                newClassNamespace,
                newClassFields
        )
    }

}