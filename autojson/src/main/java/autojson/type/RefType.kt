package autojson.type

import autojson.DebugWriter

/**
 * Created by omochi on 15/08/02.
 */

class RefType (
        val namespace: Namespace,
        val name: TypeName
): Type() {
    constructor(tuple: TupleNamespaceTypeName):
            this(tuple.namespace, tuple.name)
    {
    }

    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("namespace=$namespace")
        w.writeLine("name=$name")
    }

    val namespaceNameTuple: TupleNamespaceTypeName = TupleNamespaceTypeName(namespace, name)

    override fun applySubsts(substs: List<NamespaceEntrySubst>): RefType {
        return RefType(namespaceNameTuple.applySubsts(substs))
    }
}