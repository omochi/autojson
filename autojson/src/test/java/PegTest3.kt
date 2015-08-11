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

public class PegTest3 {
    private fun createParser(): Parser<List<Any>> {
        val E = ParserRef<List<Any>>()
        val A = ParserRef<List<Any>>()
        val AS = ParserRef<List<Any>>()
        val M = ParserRef<List<Any>>()
        val MS = ParserRef<List<Any>>()
        val P = ParserRef<Any>()
        val Number = regex("[0-9]+")

        E.value = A

        A.value = flatSeq(M.wrap(), AS) / M.wrap()
        AS.value = flatSeq(literal("+").wrap(), A) /
                flatSeq(literal("-").wrap(), A)

        M.value = flatSeq(P.wrap(), MS) / P.wrap()
        MS.value = flatSeq(literal("*").wrap(), M) /
                flatSeq(literal("/").wrap(), M) /
                flatSeq(literal("%").wrap(), M)

        P.value = Number / seq(literal("("), E, literal(")")).map { it[1] }

        return E
    }
    Test public fun test1() {
        val parser = createParser()
        val ret = parser.parse(Source("1+2-3+4*5/6*(7+8-9)"))
        assertThat(ret, `is`(instanceOf(javaClass<peg.Ok<*>>())))
        val retOk = ret as peg.Ok
        assertThat(retOk.value, `is`(
                listOf<Any>(
                        listOf<Any>("1"),
                        "+",
                        listOf<Any>("2"),
                        "-",
                        listOf<Any>("3"),
                        "+",
                        listOf<Any>(
                                "4",
                                "*",
                                "5",
                                "/",
                                "6",
                                "*",
                                listOf<Any>(
                                        listOf<Any>("7"),
                                        "+",
                                        listOf<Any>("8"),
                                        "-",
                                        listOf<Any>("9")
                                )
                        )
                ))
        )
    }
}