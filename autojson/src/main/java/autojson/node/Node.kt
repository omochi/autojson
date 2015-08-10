package autojson.node

import autojson.DebugWritable
import autojson.DebugWriter
import autojson.core.Either
import autojson.json.Json
import autojson.json.jsonArrayOf
import autojson.json.jsonObjectOf
import autojson.json.toJson
import java.util.*

/**
 * Created by omochi on 15/07/30.
 */

abstract class Node: DebugWritable {
    val pos: NodePos
    constructor(pos: NodePos) {
        this.pos = pos
    }
    override fun debugWrite(writer: DebugWriter) {
        val w = writer
        w.indent("${javaClass.getSimpleName()}(", ")") {
            w.writeLine("pos=$pos")
            writeDebugBody(w)
        }
    }
    abstract fun writeDebugBody(w: DebugWriter)

    companion object {
        fun typeFromJson(json: Json, pos: NodePos): Either<Exception, Node> {
            val string = json.asString
            if (string != null) {
                return typeFromJson(
                        jsonArrayOf("Ref".toJson(), string.toJson()),
                        pos
                )
            }

            if (json.valueType != Json.ValueType.List) {
                return Either.left(Exception(
                        "invalid type json: ${json.valueType}, pos=$pos"))
            }

            val objectTypeJson = json[0]
            val objectType = objectTypeJson.asString ?:
                    return Either.left(Exception("type is ${objectTypeJson}, pos=$pos"))
            when (objectType) {
                "Ref" -> {
                    return Either.right(RefNode.fromJson(json, pos).toRight {
                        return Either.left(Exception(
                                "decode Ref failed, pos=$pos", it
                        ))
                    })
                }
                "Class" -> {
                    return Either.right(ClassDefNode.fromJson(json, pos).toRight {
                        return Either.left(Exception(
                                "decode ClassDef failed, pos=$pos", it
                        ))
                    })
                }
                "Apply" -> {
                    return Either.right(ApplyNode.fromJson(json, pos).toRight {
                        return Either.left(Exception(
                                "decode Apply failed, pos=$pos", it
                        ))
                    })
                }
            }

            return Either.left(Exception(
                    "invalid object type: ${objectType}, pos=$pos"
            ))
        }

        fun typesFromJson(
                json: Json,
                pos: NodePos
        ): Either<Exception, Map<String, Node>> {
            val typesJsonMap = json.asMap ?: emptyMap()

            var table = LinkedHashMap<String, Node>()

            for ((name, typeJson) in typesJsonMap) {
                val node = typeFromJson(
                        typeJson,
                        pos + name
                ).toRight { return Either.left(Exception(
                        "read type failed: name=$name, pos=$pos", it)) }

                table[name] = node
            }

            return Either.right(table)
        }
    }
}