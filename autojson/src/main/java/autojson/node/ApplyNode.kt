package autojson.node

import autojson.DebugWriter
import autojson.core.Either
import autojson.json.Json

/**
 * Created by omochi on 15/07/30.
 */

class ApplyNode(
        val target: Node,
        val params: List<Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("target=", false)
        w.writeObject(target)
        w.indent("params=[", "]") {
            for (param in params) {
                w.writeObject(param)
            }
        }
    }

    companion object {
        fun fromJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, ApplyNode> {
            val target = Node.typeFromJson(json[1], pos + "target").toRight {
                return Either.left(Exception("target is null, pos=$pos"))
            }

            val params = json[2].asList?.withIndex()?.map {
                val (index, paramNode) = it
                Node.typeFromJson(paramNode, pos + "params[$index]").toRight {
                    return Either.left(Exception("params[$index] is null: pos=$pos", it))
                }
            } ?: return Either.left(Exception("params is null, pos=$pos"))

            return Either.right(ApplyNode(
                    target,
                    params,
                    pos
            ))
        }
    }
}