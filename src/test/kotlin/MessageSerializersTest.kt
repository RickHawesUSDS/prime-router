package gov.cdc.prime.router

import kotlin.test.*

class MessageSerializersTest {
    @Test
    fun `test isValidCsv`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val csvGood= listOf(listOf("a", "b"), listOf("1", "2"))
        val csvExtra= listOf(listOf("a", "b", "c"), listOf("1", "2", "3"))
        val csvBad= listOf(listOf("c", "b"), listOf("1", "2"))
        assertEquals(true, Message.isValidCsv(one, csvGood))
        assertEquals(true, Message.isValidCsv(one, csvExtra))
        assertEquals(false, Message.isValidCsv(one, csvBad))
    }

    @Test
    fun `test buildColumnMap`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val csvGood= listOf(listOf("a", "b"), listOf("1", "2"))
        assertNotNull(Message.buildColumnMap(one, csvGood))
        val csvReverse= listOf(listOf("b", "c", "a"), listOf("1", "2", "3"))
        assertNotNull(Message.buildColumnMap(one, csvReverse))
    }

    @Test
    fun `test decodeCsv`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))

        val csvGood= listOf(listOf("a", "b"), listOf("1", "2"), listOf("3", "4"))
        val messages = Message.decodeCsv(one, csvGood)
        assertEquals(2, messages.size)
        assertEquals("3", messages[1].values["a"])
        assertEquals("2", messages[0].values["b"])

        val csvReverse= listOf(listOf("b", "a"), listOf("2", "1"), listOf("4", "3"))
        val messagesReversed = Message.decodeCsv(one, csvReverse)
        assertEquals(2, messagesReversed.size)
        assertEquals("3", messagesReversed[1].values["a"])
        assertEquals("2", messagesReversed[0].values["b"])
    }


    @Test
    fun `test encodeCsv`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val csvGood= listOf(listOf("a", "b"), listOf("1", "2"), listOf("3", "4"))
        val messages = Message.decodeCsv(one, csvGood)
        val csvOut = Message.encodeCsv(messages)
        assertEquals(3, csvOut.size)
        assertEquals("a", csvOut[0][0])
        assertEquals("4", csvOut[2][1])
    }

}