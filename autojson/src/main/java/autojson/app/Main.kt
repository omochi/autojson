package autojson.app

import java.io.File
import kotlin.platform.platformStatic

/**
 * Created by omochi on 15/07/28.
 */

object Main {
    platformStatic
    fun main(args: Array<String>) {
        val cwd = File(".").getAbsolutePath()
        println("hello kotlin world: $cwd")
        for (arg in args) {
            println(arg)
        }
    }
}