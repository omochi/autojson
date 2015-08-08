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

        return SourceFileNode.fromJson(json, NodePos(file, emptyList()))
    }

}