package autojson

import autojson.core.Either
import autojson.core.MutableBox
import autojson.core.mutableBoxOf
import autojson.json.Json
import autojson.json.jsonObjectOf
import autojson.json.toJson
import autojson.node.*
import autojson.type.*
import java.io.File
import java.util.*

/**
 * Created by omochi on 15/07/29.
 */

class SourceFileReader {
    fun read(file: File): Either<Exception, SourceFileNode> {
        val jsonStr = file.readText()
        val json = Json.parse(jsonStr).toRight{
            return Either.left(Exception(
                "json parse failed", it)) }

        return readFromJson(json, NodePos(file, emptyList()))
    }

//    private fun getBuiltInTypes(): TypeTable {
//        var table = TypeTable()
//
//        table = table.addType("Bool",
//                ClassDefNode("Bool", emptyList())
//        )
//        table = table.addType("Int",
//                ClassDefNode("Int", emptyList())
//        )
//        table = table.addType("String",
//                ClassDefNode("String", emptyList())
//        )
//        table = table.addType("List",
//                LetNode(
//                        listOf("element"),
//                        ClassDefNode("List", emptyList())
//                        )
//        )
//
//        return table
//    }

    private fun readFromJson(json: Json, pos: NodePos): Either<Exception, SourceFileNode> {
        val types = readTypesJson(json["types"], pos + "types").switch(
                { return Either.left(it) },
                { it }
        )
        return Either.right(
                SourceFileNode(
                        types,
                        pos
                )
        )
    }
    private fun readTypesJson(
            typesJson: Json,
            pos: NodePos
    ): Either<Exception, Map<String, Node>> {
        val typesJsonMap = typesJson.asMap ?:
                return Either.left(Exception(
                        "types json is ${typesJson.valueType}: pos=$pos"))

        var table = LinkedHashMap<String, Node>()

        for ((name, typeJson) in typesJsonMap) {
            val node = readTypeJson(
                    typeJson,
                    pos + name
            ).toRight { return Either.left(Exception(
                    "read type failed: name=$name, pos=$pos", it)) }

            table[name] = node
        }

        return Either.right(table)
    }

    private fun readTypeJson(
            typeJson: Json,
            pos: NodePos
    ): Either<Exception, Node> {
        val string = typeJson.asString
        if (string != null) {
            return readTypeJson(
                    jsonObjectOf(
                            "type" to "Ref".toJson(),
                            "name" to string.toJson()
                    ),
                    pos
            )
        }

        if (typeJson.valueType != Json.ValueType.Map) {
            return Either.left(Exception(
                    "invalid type json: ${typeJson.valueType}, pos=$pos"))
        }

        val objectTypeJson = typeJson["type"]
        val objectType = objectTypeJson.asString ?:
                    return Either.left(Exception("type is ${objectTypeJson}, pos=$pos"))
        when (objectType) {
            "Ref" -> {
                return Either.right(readRefJson(typeJson, pos).toRight {
                    return Either.left(Exception(
                            "broken Ref, pos=$pos", it
                    ))
                })
            }
            "Class" -> {
                return Either.right(readClassDefJson(typeJson, pos).toRight {
                    return Either.left(Exception(
                            "broken Class, pos=$pos", it
                    ))
                })
            }
            "Apply" -> {
                return Either.right(readApplyJson(typeJson, pos).toRight {
                    return Either.left(Exception(
                            "broken Apply, pos=$pos", it
                    ))
                })
            }
        }

        return Either.left(Exception(
                "invalid object type: ${objectType}, pos=$pos"
        ))
    }

    private fun readRefJson(
            json: Json,
            pos: NodePos
    ): Either<Exception, RefNode> {
        val name = json["name"].asString ?:
                return Either.left(Exception(
                        "name is null, pos=$pos"))
        return Either.right(RefNode(name, pos))
    }

    private fun readLetJson(
            json: Json,
            pos: NodePos
    ): Either<Exception, List<String>> {
        val let = json.asList?.map {
            it.asString ?:
                    return Either.left(Exception(
                            "let element is null: pos=$pos"))
        } ?:
                return Either.left(Exception("let is null: pos=$pos"))

        return Either.right(let)
    }

    private fun readClassDefJson(
            json: Json,
            pos: NodePos
    ): Either<Exception, ClassDefNode> {
        val let = readLetJson(json["let"], pos + "let").toRight { emptyList() }

        val name = json["name"].asString

        val fields = json["fields"].asMap?.map {
            val name = it.key
            val type = readTypeJson(it.value, pos + "fields" + name).toRight {
                return Either.left(Exception(
                        "read field failed: name=$name, pos=$pos", it
                ))
            }
            name to type
        }?.toMap() ?:
                return Either.left(Exception("fields is null, pos=$pos"))

        return Either.right(ClassDefNode(let, name, fields, pos))
    }

    private fun readApplyJson(
            json: Json,
            pos: NodePos
    ): Either<Exception, ApplyNode> {
        val let = readLetJson(json["let"], pos + "let").toRight { emptyList() }

        val name = json["name"].asString

        val target = readTypeJson(json["target"], pos + "target").toRight {
            return Either.left(Exception("target is null, pos=$pos"))
        }

        val params = json["params"].asMap?.map {
            val name = it.getKey()
            val param = readTypeJson(it.value, pos + "params" + name).toRight {
                return Either.left(Exception("param is null: name=$name, pos=$pos", it))
            }
            name to param
        }?.toMap() ?:
                return Either.left(Exception("params is null, pos=$pos"))

        return Either.right(ApplyNode(
                let,
                name,
                target,
                params,
                pos
        ))
    }
}