package autojson.type

/**
 * Created by omochi on 15/08/06.
 */

data class TupleNamespaceType(
        val namespace: Namespace,
        val type: Type
)

data class TupleNamespaceClassType(
        val namespace: Namespace,
        val type: ClassType
)

data class TupleNamespaceTypeName(
        val namespace: Namespace,
        val name: TypeName
) {
    fun applySubsts(
            substs: List<NamespaceEntrySubst>
    ): TupleNamespaceTypeName {
        for (subst in substs) {
            if (this == subst.source) {
                return subst.dest
            }
        }
        return this
    }
}