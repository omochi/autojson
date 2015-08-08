package autojson.node

import autojson.DebugWriter
import autojson.core.Either
import autojson.json.Json

/**
 * Created by omochi on 15/07/30.
 */

class ClassDefNode (
        val let: List<String>,
        val name: String?,
        val fields: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("let=$let")
        w.writeLine("name=$name")
        w.indent("fields={", "}") {
            for ((name, field) in fields) {
                w.writeLine("$name=", false)
                w.writeObject(field)
            }
        }
    }

    companion object {
        fun fromJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, ClassDefNode> {
            val let = letFromJson(json["let"], pos + "let").toRight { emptyList() }

            val name = json["name"].asString

            val fields = json["fields"].asMap?.map {
                val name = it.key
                val type = Node.typeFromJson(it.value, pos + "fields" + name).toRight {
                    return Either.left(Exception(
                            "read field failed: name=$name, pos=$pos", it
                    ))
                }
                name to type
            }?.toMap() ?: emptyMap()

            return Either.right(ClassDefNode(let, name, fields, pos))
        }

        fun letFromJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, List<String>> {
            val let = json.asList?.map {
                it.asString ?:
                        return Either.left(Exception(
                                "let element is null: pos=$pos"))
            } ?: return Either.left(Exception("let is null: pos=$pos"))

            return Either.right(let)
        }
    }
}