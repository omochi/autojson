import autojson.SourceFileReader
import autojson.json.Json
import autojson.node.SourceFileNode
import autojson.type.Namespace
import autojson.type.NodeType
import autojson.type.TypeResolver
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.junit.Test
import java.io.File
import java.io.Reader
import java.io.StringReader
import java.util.*

/**
 * Created by omochi on 15/07/28.
 */

public class TemporaryTest {
    private fun readSourceFile(file: File): SourceFileNode {
        val reader = SourceFileReader()
        return reader.read(file).toRight { throw it }
    }

    Test public fun test1() {
        val source = readSourceFile(File("./res/test/test1.json"))

        val resolver = TypeResolver()
        val namespace = resolver.decodeFile(source).toRight { throw it }

        println("test1 namespace")
        print(namespace.toDebugString())

        println("===")
    }

    Test public fun test2() {
        val source = readSourceFile(File("./res/test/test2.json"))

        val resolver = TypeResolver()
        val namespace = resolver.decodeFile(source).toRight { throw it }

        println("test2 namespace")
        print(namespace.toDebugString())

        println("===")
    }

    Test public fun test3() {
        val source = readSourceFile(File("./res/test/test3.json"))

        val resolver = TypeResolver()
        val namespace = resolver.decodeFile(source).toRight { throw it }

        println("test3 namespace")
        print(namespace.toDebugString())

        println("===")
    }
}