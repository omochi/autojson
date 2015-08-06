package autojson

/**
 * Created by omochi on 15/07/30.
 */

class DebugWriter {
    var nest: Int
    var stringBuilder: StringBuilder
    var indented: Boolean
    constructor() {
        this.nest = 0
        this.stringBuilder = StringBuilder()
        this.indented = false
    }
    fun push() {
        nest += 1
    }
    fun pop() {
        nest -= 1
    }

    fun indent(head: String, foot: String, proc: ()-> Unit) {
        writeLine(head, true)
        push()
        proc()
        pop()
        writeLine(foot, true)
    }
    fun writeObject(writable: DebugWritable) {
        writable.debugWrite(this)
    }
    fun writeLine(line: String, newLine: Boolean = true) {
        if (!indented) {
            for (i in 0..nest - 1) {
                stringBuilder.append("  ")
            }
            indented = true
        }
        stringBuilder.append(line)
        if (newLine) {
            writeNewline()
        }
    }
    fun writeNewline() {
        stringBuilder.append("\n")
        indented = false
    }

    fun getString(): String {
        return stringBuilder.toString()
    }
}