package gov.cdc.prime.router

import java.io.*
import java.nio.charset.StandardCharsets
import kotlin.test.*

class MappableTableTests {


    @Test
    fun `test concat`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("example1", one, listOf(listOf("1", "2"), listOf("3", "4")))
        val table2 = MappableTable("example2", one, listOf(listOf("5", "6"), listOf("7", "8")))
        val concatTable = table1.concat("concat", table2)
        assertEquals(4, concatTable.rowCount)
        assertEquals(2, table1.rowCount)
        assertEquals("8", concatTable.getString(3, "b"))
    }

    @Test
    fun `test filter`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("example", one, listOf(listOf("1", "2"), listOf("3", "4")))
        assertEquals(2, table1.rowCount)
        val filteredTable = table1.filter("filtered", mapOf("a" to "1"))
        assertEquals("filtered", filteredTable.name)
        assertEquals(one, filteredTable.schema)
        assertEquals(1, filteredTable.rowCount)
        assertEquals("2", filteredTable.getString(0, "b"))
    }

    @Test
    fun `test filterByReceiver with One Receiver`() {
        val schema1 = Schema(name = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("one", schema1, listOf(listOf("X", "1"), listOf("Y", "2"), listOf("Z", "3")))
        val receiver1 = Receiver(
            name = "rec1",
            schema = "test",
            patterns = mapOf("a" to "Y|Z")
        )
        val tables = table1.filterByReceiver(listOf(receiver1))
        assertEquals(1, tables.size)
        assertEquals("rec1-one", tables[0].name)
        assertEquals(2, tables[0].rowCount)
    }

    @Test
    fun `test filterByReceiver with Multiple Receivers`() {
        val schema1 = Schema(name = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val table1 = MappableTable("one", schema1, listOf(listOf("X", "1"), listOf("Y", "2"), listOf("Z", "3")))
        val receiver1 = Receiver(
            name = "rec1",
            schema = "test",
            patterns = mapOf("a" to "X")
        )
        val receiver2 = Receiver(
            name = "rec2",
            schema = "test",
            patterns = mapOf("a" to "Y|Z")
        )
        val receiver3 = Receiver(
            name = "rec3",
            schema = "test",
            patterns = mapOf("a" to "W")
        )
        val tables = table1.filterByReceiver(listOf(receiver1, receiver2, receiver3))
        assertEquals(3, tables.size)
        assertEquals("rec1-one", tables[0].name)
        assertEquals("rec2-one", tables[1].name)
        assertEquals(1, tables[0].rowCount)
        assertEquals(2, tables[1].rowCount)
    }

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