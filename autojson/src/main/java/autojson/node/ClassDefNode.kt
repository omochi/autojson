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
        val types: Map<String, Node>,
        val fields: Map<String, Node>,
        pos: NodePos
): Node(pos) {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("let=$let")
        w.writeLine("name=$name")
        w.writeLine("types=", false)
        w.writeMap(types)
        w.writeLine("fields=", false)
        w.writeMap(fields)
    }

    companion object {
        fun fromJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, ClassDefNode> {
            val body = json[1]
            if (body.valueType != Json.ValueType.Map) {
                return Either.left(Exception("class def body is not map: type=${body.valueType}"))
            }
            return fromBodyJson(body, pos)
        }

        fun fromBodyJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, ClassDefNode> {
            val let = letFromJson(json["let"], pos + "let").toRight { emptyList() }

            val name = json["name"].asString

            val types = Node.typesFromJson(json["types"], pos + "types").toRight {
                return Either.left(it)
            }

            val fields = json["fields"].asMap?.map {
                val fieldName = it.key
                val type = Node.typeFromJson(it.value, pos + "fields" + fieldName).toRight {
                    return Either.left(Exception(
                            "read field failed: name=$fieldName, pos=$pos", it
                    ))
                }
                fieldName to type
            }?.toMap() ?: emptyMap()

            return Either.right(ClassDefNode(let, name, types, fields, pos))
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