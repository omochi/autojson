package autojson.node

import autojson.DebugWriter
import autojson.core.Either
import autojson.json.Json
import autojson.node.Node

/**
 * Created by omochi on 15/07/28.
 */

class RefNode(
        val name: String,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("name=$name")
    }

    companion object {
        fun fromJson(json: Json, pos: NodePos): Either<Exception, RefNode> {
            val name = json["name"].asString ?:
                    return Either.left(Exception(
                            "name is null, pos=$pos"))
            return Either.right(RefNode(name, pos))
        }
    }
}