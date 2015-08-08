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
)