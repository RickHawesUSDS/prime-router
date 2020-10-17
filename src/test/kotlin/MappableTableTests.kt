package gov.cdc.prime.router

import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.test.*

class MappableTableTests {
    /*
    @Test
    fun `test patternMatch`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("test", one, listOf(listOf("1", "2")))
        assertEquals (true, table1.patternMatch(mapOf("a" to "1"))
        assertEquals(false, table1.patternMatch(mapOf("a" to "2")))
        assertEquals(true, table1.patternMatch(mapOf("a" to ".*")))
        assertEquals(true, table1.patternMatch(mapOf("a" to "1|2")))
        assertEquals(true, table1.patternMatch(mapOf("a" to "1", "b" to "2")))
        assertEquals(false, table1.patternMatch(mapOf("a" to "1", "b" to "3")))
    }
    */

    /*
    @Test
    fun `test split one message`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val dataSet1 = MappableTable("example", one, mapOf("a" to "1", "b" to "2"))
        val rec1 = Receiver(
            "phd1",
            topics = listOf(Receiver.Topic(schema = "one", patterns = mapOf("a" to "1"), address = "foo"))
        )
        val split = MappableTable.splitMessages(listOf(dataSet1), receivers = mapOf(rec1.name to rec1))
        assertNotNull(split[rec1.name])
        assertEquals(1, split[rec1.name]?.size)
        assertEquals(dataSet1, split[rec1.name]?.get(0))
    }

    @Test
    fun `test split two messages`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val dataSet1 = MappableTable("test1", one, mapOf("a" to "1", "b" to "2"))
        val dataSet2 = MappableTable("test2", one, mapOf("a" to "3", "b" to "4"))
        val rec1 = Receiver(
            "phd1",
            topics = listOf(Receiver.Topic(schema = "one", patterns = mapOf("a" to "1"), address = "foo"))
        )
        val split = MappableTable.splitMessages(listOf(dataSet1, dataSet2), receivers = mapOf(rec1.name to rec1))
        assertNotNull(split[rec1.name])
        assertEquals(1, split[rec1.name]?.size)
        assertEquals(dataSet1, split[rec1.name]?.get(0))
    }

    @Test
    fun `test split two messages two ways`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val dataSet1 = MappableTable("test1", one, mapOf("a" to "1", "b" to "2"))
        val dataSet2 = MappableTable("test2", one, mapOf("a" to "3", "b" to "4"))
        val rec1 = Receiver(
            "phd1",
            topics = listOf(Receiver.Topic(schema = "one", patterns = mapOf("a" to "1"), address = "foo"))
        )
        val rec2 = Receiver(
            "phd2",
            topics = listOf(Receiver.Topic(schema = "one", patterns = mapOf("a" to ".*"), address = "food"))
        )
        val split = MappableTable.splitMessages(
            listOf(dataSet1, dataSet2),
            receivers = mapOf(rec1.name to rec1, rec2.name to rec2)
        )
        assertNotNull(split[rec1.name])
        assertEquals(1, split[rec1.name]?.size)
        assertEquals(dataSet1, split[rec1.name]?.get(0))
        assertNotNull(split[rec2.name])
        assertEquals(2, split[rec2.name]?.size)
        assertEquals(dataSet1, split[rec2.name]?.get(0))
    }

    // CSV Tests

    @Test
    fun `test isValidCsv`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val csvGood = listOf(listOf("a", "b"), listOf("1", "2"))
        val csvExtra = listOf(listOf("a", "b", "c"), listOf("1", "2", "3"))
        val csvBad = listOf(listOf("c", "b"), listOf("1", "2"))
        assertEquals(true, MappableTable.isValidCsv(one, csvGood))
        assertEquals(true, MappableTable.isValidCsv(one, csvExtra))
        assertEquals(false, MappableTable.isValidCsv(one, csvBad))
    }

    @Test
    fun `test buildColumnMap`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val csvGood = listOf(listOf("a", "b"), listOf("1", "2"))
        assertNotNull(MappableTable.buildColumnMap(one, csvGood))
        val csvReverse = listOf(listOf("b", "c", "a"), listOf("1", "2", "3"))
        assertNotNull(MappableTable.buildColumnMap(one, csvReverse))
    }
    */

    @Test
    fun `test isEmpty`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val emptyTable = MappableTable("test", one)
        assertEquals(true, emptyTable.isEmpty())
        val table1 = MappableTable("test", one, listOf(listOf("1", "2")))
        assertEquals(false, table1.isEmpty())
    }

    @Test
    fun `test create with list`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("test", one, listOf(listOf("1", "2")))
        assertEquals("test", table1.name)
        assertEquals(one, table1.schema)
        assertEquals(true, table1.isRaw)
        assertEquals(1, table1.rowCount)
    }

    @Test
    fun `test create from csv`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val csv = """
            a,b
            1,2
        """.trimIndent()

        val table1 = MappableTable("test", one, ByteArrayInputStream(csv.toByteArray()), MappableTable.StreamType.CSV)
        assertEquals(true, table1.isRaw)
        assertEquals(1, table1.rowCount)
    }

    @Test
    fun `test write as csv`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("test", one, listOf(listOf("1", "2")))
        val expectedCsv = """
            a,b
            1,2
            
        """.trimIndent()
        val output = ByteArrayOutputStream()
        table1.write(output, MappableTable.StreamType.CSV)
        assertEquals(expectedCsv, output.toString(StandardCharsets.UTF_8))
    }

}