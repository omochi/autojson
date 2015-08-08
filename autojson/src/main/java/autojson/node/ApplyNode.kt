package autojson.node

import autojson.DebugWriter
import autojson.core.Either
import autojson.json.Json

/**
 * Created by omochi on 15/07/30.
 */

class ApplyNode(
        val name: String?,
        val target: Node,
        val params: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("name=$name")
        w.writeLine("target=", false)
        w.writeObject(target)
        w.indent("params={", "}") {
            for ((name, param) in params) {
                w.writeLine("$name=", false)
                w.writeObject(param)
            }
        }
    }

    companion object {
        fun fromJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, ApplyNode> {
            val name = json["name"].asString

            val target = Node.typeFromJson(json["target"], pos + "target").toRight {
                return Either.left(Exception("target is null, pos=$pos"))
            }

            val params = json["params"].asMap?.map {
                val name = it.getKey()
                val param = Node.typeFromJson(it.value, pos + "params" + name).toRight {
                    return Either.left(Exception("param is null: name=$name, pos=$pos", it))
                }
                name to param
            }?.toMap() ?:
                    return Either.left(Exception("params is null, pos=$pos"))

            return Either.right(ApplyNode(
                    name,
                    target,
                    params,
                    pos
            ))
        }
    }
}