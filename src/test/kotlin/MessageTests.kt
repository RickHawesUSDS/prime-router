package gov.cdc.prime.router

import kotlin.test.*

class MessageTests {
    @Test
    fun `test patternMatch`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val msg1 = Message(one, mapOf("a" to "1", "b" to "2"))
        assertEquals(true, msg1.patternMatch(mapOf("a" to "1")))
        assertEquals(false, msg1.patternMatch(mapOf("a" to "2")))
        assertEquals(true, msg1.patternMatch(mapOf("a" to ".*")))
        assertEquals(true, msg1.patternMatch(mapOf("a" to "1|2")))
        assertEquals(true, msg1.patternMatch(mapOf("a" to "1", "b" to "2")))
        assertEquals(false, msg1.patternMatch(mapOf("a" to "1", "b" to "3")))
    }

    @Test
    fun `test split one message`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val msg1 = Message(one, mapOf("a" to "1", "b" to "2"))
        val rec1 = Receiver("phd1", topics = listOf(Receiver.Topic(patterns = mapOf("a" to "1"))))
        val split = Message.splitMessages(listOf(msg1), receivers = mapOf(rec1.name to rec1))
        assertNotNull(split[rec1.name])
        assertEquals(1, split[rec1.name]?.size)
        assertEquals(msg1, split[rec1.name]?.get(0))
    }

    @Test
    fun `test split two messages`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val msg1 = Message(one, mapOf("a" to "1", "b" to "2"))
        val msg2 = Message(one, mapOf("a" to "3", "b" to "4"))
        val rec1 = Receiver("phd1", topics = listOf(Receiver.Topic(patterns = mapOf("a" to "1"))))
        val split = Message.splitMessages(listOf(msg1, msg2), receivers = mapOf(rec1.name to rec1))
        assertNotNull(split[rec1.name])
        assertEquals(1, split[rec1.name]?.size)
        assertEquals(msg1, split[rec1.name]?.get(0))
    }

    @Test
    fun `test split two messages two ways`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val msg1 = Message(one, mapOf("a" to "1", "b" to "2"))
        val msg2 = Message(one, mapOf("a" to "3", "b" to "4"))
        val rec1 = Receiver("phd1", topics = listOf(Receiver.Topic(patterns = mapOf("a" to "1"))))
        val rec2 = Receiver("phd2", topics = listOf(Receiver.Topic(patterns = mapOf("a" to ".*"))))
        val split = Message.splitMessages(listOf(msg1, msg2), receivers = mapOf(rec1.name to rec1, rec2.name to rec2))
        assertNotNull(split[rec1.name])
        assertEquals(1, split[rec1.name]?.size)
        assertEquals(msg1, split[rec1.name]?.get(0))
        assertNotNull(split[rec2.name])
        assertEquals(2, split[rec2.name]?.size)
        assertEquals(msg1, split[rec2.name]?.get(0))
    }

    @Test
    fun `test isValidCsv`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val csvGood = listOf(listOf("a", "b"), listOf("1", "2"))
        val csvExtra = listOf(listOf("a", "b", "c"), listOf("1", "2", "3"))
        val csvBad = listOf(listOf("c", "b"), listOf("1", "2"))
        assertEquals(true, Message.isValidCsv(one, csvGood))
        assertEquals(true, Message.isValidCsv(one, csvExtra))
        assertEquals(false, Message.isValidCsv(one, csvBad))
    }

    @Test
    fun `test buildColumnMap`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val csvGood = listOf(listOf("a", "b"), listOf("1", "2"))
        assertNotNull(Message.buildColumnMap(one, csvGood))
        val csvReverse = listOf(listOf("b", "c", "a"), listOf("1", "2", "3"))
        assertNotNull(Message.buildColumnMap(one, csvReverse))
    }

    @Test
    fun `test decodeCsv`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))

        val csvGood = listOf(listOf("a", "b"), listOf("1", "2"), listOf("3", "4"))
        val messages = Message.decodeCsv(one, csvGood)
        assertEquals(2, messages.size)
        assertEquals("3", messages[1].values["a"])
        assertEquals("2", messages[0].values["b"])

        val csvReverse = listOf(listOf("b", "a"), listOf("2", "1"), listOf("4", "3"))
        val messagesReversed = Message.decodeCsv(one, csvReverse)
        assertEquals(2, messagesReversed.size)
        assertEquals("3", messagesReversed[1].values["a"])
        assertEquals("2", messagesReversed[0].values["b"])
    }


    @Test
    fun `test encodeCsv`() {
        val one = Schema(name = "one", elements = listOf(Element("a"), Element("b")))
        val csvGood = listOf(listOf("a", "b"), listOf("1", "2"), listOf("3", "4"))
        val messages = Message.decodeCsv(one, csvGood)
        val csvOut = Message.encodeCsv(messages)
        assertEquals(3, csvOut.size)
        assertEquals("a", csvOut[0][0])
        assertEquals("4", csvOut[2][1])
    }

}