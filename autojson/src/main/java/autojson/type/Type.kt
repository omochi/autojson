package autojson.type

import autojson.DebugWritable
import autojson.DebugWriter

/**
 * Created by omochi on 15/07/31.
 */

abstract class Type: DebugWritable {
    override fun debugWrite(writer: DebugWriter) {
        val w = writer
        w.indent("${javaClass.getSimpleName()}(", ")") {
            writeDebugBody(w)
        }
    }
    abstract fun writeDebugBody(w: DebugWriter)
}