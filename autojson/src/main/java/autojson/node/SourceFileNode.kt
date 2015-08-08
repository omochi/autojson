package autojson.node

import autojson.DebugWriter
import autojson.core.Either
import autojson.json.Json

/**
 * Created by omochi on 15/07/30.
 */

class SourceFileNode (
        val types: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.indent("types={", "}") {
            for ((name, type) in types) {
                w.writeLine("$name=", false)
                w.writeObject(type)
            }
        }
    }

    companion object {
        fun fromJson(json: Json, pos: NodePos): Either<Exception, SourceFileNode> {
            val types = Node.typesFromJson(json["types"], pos + "types").toRight {
                return Either.left(it)
            }

            return Either.right(
                    SourceFileNode(
                            types,
                            pos
                    )
            )
        }
    }
}