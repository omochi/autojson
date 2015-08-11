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

public class PegTest1 {
    Test public fun testLiteral1() {
        val ret = literal("apple").parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<Ok<*>>())))
    }
    Test public fun testLiteral2() {
        val ret = literal("apple").parse(Source("appl"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Error>())))
    }
    Test public fun testLiteral3() {
        val ret = literal("apple").parse(Source("appll"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Error>())))
    }
    Test public fun testEmpty1() {
        val ret = empty(Unit).parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<Ok<*>>())))
    }
    Test public fun testEmpty2() {
        val ret = empty(Unit).parse(Source(""))
        assertThat(ret, `is`(instanceOf(javaClass<Ok<*>>())))
    }
    Test public fun testDot1() {
        val ret = dot().parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<Ok<*>>())))
    }
    Test public fun testDot2() {
        val ret = dot().parse(Source(""))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Error>())))
    }
    Test public fun testRegex1() {
        val ret = regex("ap").parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<Ok<*>>())))
    }
    Test public fun testRegex2() {
        val ret = regex("pl").parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Error>())))
    }
    Test public fun testSeq1() {
        val ret = seq(
                literal("a"),
                literal("pp"),
                literal("le")
        ).parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
    }
    Test public fun testChoice1() {
        val ret = (
                literal("banana") / literal("apple")
        ).parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
    }
    Test public fun testOpt1() {
        val ret = literal("banana").opt().parse(Source("apple"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
    }
    Test public fun testZeroOrMore1() {
        val ret = literal("ab").zeroOrMore().parse(Source("ababab"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("ab", "ab", "ab")))
    }
    Test public fun testZeroOrMore2() {
        val ret = literal("aba").zeroOrMore().parse(Source("ababab"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("aba")))
    }
    Test public fun testZeroOrMore3() {
        val ret = literal("aa").zeroOrMore().parse(Source("ababab"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(emptyList()))
    }
    Test public fun testOneOrMore1() {
        val source = Source("ababab")
        val ret = literal("ab").oneOrMore().parse(source)
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("ab", "ab", "ab")))
    }
    Test public fun testOneOrMore2() {
        val ret = literal("aba").oneOrMore().parse(Source("ababab"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("aba")))
    }
    Test public fun testOneOrMore3() {
        val ret = literal("aa").oneOrMore().parse(Source("ababab"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Error>())))
    }
    Test public fun testAndPred1() {
        val ret = seq(
                andPred(literal("aa")).map { "" },
                literal("aabb")
        ).parse(Source("aabb"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("", "aabb")))
    }
    Test public fun testNotPred1() {
        val ret = seq(
                notPred(literal("bb")).map { "" },
                literal("aabb")
        ).parse(Source("aabb"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(listOf("", "aabb")))
    }
}