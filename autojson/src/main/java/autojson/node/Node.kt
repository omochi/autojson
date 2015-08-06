package autojson.node

import autojson.DebugWritable
import autojson.DebugWriter

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
}