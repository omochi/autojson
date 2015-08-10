package autojson.type

import autojson.DebugWriter

/**
 * Created by omochi on 15/08/02.
 */

class RefType (
        val namespace: Namespace,
        val name: TypeName
): Type() {
    override fun toString(): String {
        return "RefType(name=$name, namespace=$namespace)"
    }

    constructor(tuple: TupleNamespaceTypeName):
            this(tuple.namespace, tuple.name)
    {
    }

    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("namespace=$namespace")
        w.writeLine("name=$name")
    }

    val namespaceNameTuple: TupleNamespaceTypeName = TupleNamespaceTypeName(namespace, name)

    override fun applySubsts(subst: NameSubstTable): RefType {
        return RefType(namespaceNameTuple.applySubsts(subst))
    }
}