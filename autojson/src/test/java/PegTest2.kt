/**
 * Created by omochi on 15/08/12.
 */
import autojson.node.SourceFileNode
import autojson.node.SourceFileReader
import autojson.peg.*
import autojson.peg as peg
import autojson.type.TypeResolver
import org.junit.Test
import java.io.File
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import kotlin.text.Regex

/**
 * Created by omochi on 15/08/11.
 */

public class PegTest2 {
    private fun createParser(): Parser<List<String>> {
        val E = ParserRef<List<String>>()
        var V = ParserRef<String>()

        E.value = flatSeq(
                V.wrap(),
                literal("+").wrap(),
                E
        ) / V.wrap()
        V.value = literal("a") / literal("b") / literal("c")

        return E
    }
    Test public fun test1() {
        val parser = createParser()
        println(parser)
        val ret = parser.parse(Source("a"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("a")))
    }
    Test public fun test2() {
        val parser = createParser()
        val ret = parser.parse(Source("a+b"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("a", "+", "b")))
    }
    Test public fun test3() {
        val parser = createParser()
        val ret = parser.parse(Source("a+b+c"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("a", "+", "b", "+", "c")))
    }
    Test public fun test4() {
        val parser = createParser()
        val ret = parser.parse(Source("a+c"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("a", "+", "c")))
    }
    Test public fun test5() {
        val parser = createParser()
        val ret = parser.parse(Source("a+b+c+a"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("a", "+", "b", "+", "c", "+", "a")))
    }
}