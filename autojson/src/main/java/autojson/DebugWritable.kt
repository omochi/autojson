package autojson

/**
 * Created by omochi on 15/08/02.
 */

interface DebugWritable {
    fun toDebugString(): String {
        val writer = DebugWriter()
        writer.writeObject(this)
        return writer.getString()
    }
    fun debugWrite(writer: DebugWriter)
}