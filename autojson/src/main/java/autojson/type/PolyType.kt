package autojson.type

import autojson.DebugWriter

/**
 * Created by omochi on 15/08/03.
 */

class PolyType (

): Type() {
    override fun writeDebugBody(w: DebugWriter) {
        w.writeLine("id=${System.identityHashCode(this)}")
    }
}