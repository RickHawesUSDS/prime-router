package gov.cdc.prime.router

import kotlin.test.*

class SchemaTests {
    @Test
    fun `create schema`() {
        val one = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        assertNotNull(one)
    }

    @Test
    fun `compare schemas`() {
        val one = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val oneAgain = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        val two = Schema(name = "two", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        assertEquals(one, oneAgain)
        assertNotEquals(one, two)
    }

    @Test
    fun `find element`() {
        val one = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        assertEquals(one.findElement("a"), Schema.Element("a"))
        assertNull(one.findElement("c"))
    }

    @Test
    fun `test loading schema catalog`() {
        Schema.loadSchemaCatalog("./src/test/unit_test_files")
        assertNotNull(Schema.schemas["lab_test_results_schema"])
    }

    @Test
    fun `test loading two schemas`() {
        val one = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("a")))
        val two = Schema(name = "two", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        Schema.loadSchemas(mapOf("one" to one, "two" to two))
        assertEquals(2, Schema.schemas.size)
        assertNotNull(Schema.schemas["one"])
    }

    @Test
    fun `test buildMapping`() {
        val one = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("A")))
        val two = Schema(name = "two", topic = "test", elements = listOf(Schema.Element("a"), Schema.Element("b")))

        val oneToTwo = one.buildMapping(two)
        assertEquals(one, oneToTwo.fromSchema)
        assertEquals(two, oneToTwo.toSchema)
        assertEquals(1, oneToTwo.useFromName.size)
        assertEquals("A", oneToTwo.useFromName["a"])
        assertEquals(true, oneToTwo.useDefault.contains("b"))
        assertEquals(0, oneToTwo.missing.size)

        val twoToOne = two.buildMapping(toSchema = one)
        assertEquals(1, twoToOne.useFromName.size)
        assertEquals("a", twoToOne.useFromName["A"])
        assertEquals(0, twoToOne.useDefault.size)
        assertEquals(0, twoToOne.missing.size)
    }

    @Test
    fun `test buildMapping with missing`() {
        val one = Schema(name = "one", topic = "test", elements = listOf(Schema.Element("A")))
        val three = Schema(
            name = "three",
            topic = "test",
            elements = listOf(Schema.Element("a_"), Schema.Element("c", optional = false))
        )
        val oneToThree = one.buildMapping(toSchema = three)
        assertEquals(1, oneToThree.useFromName.size)
        assertEquals("A", oneToThree.useFromName["a_"])
        assertEquals(0, oneToThree.useDefault.size)
        assertEquals(1, oneToThree.missing.size)
    }
}