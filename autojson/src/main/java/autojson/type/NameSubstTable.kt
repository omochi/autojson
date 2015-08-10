package autojson.type

import java.util.*

/**
 * Created by omochi on 15/08/10.
 */

class NameSubstTable {
    val table: MutableMap<TupleNamespaceTypeName, TupleNamespaceTypeName>

    constructor(table: Map<TupleNamespaceTypeName, TupleNamespaceTypeName> = emptyMap()) {
        this.table = LinkedHashMap(table)
    }
    constructor(other: NameSubstTable): this(other.table) {

    }
}