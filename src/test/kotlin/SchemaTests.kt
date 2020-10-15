package gov.cdc.prime.router

import kotlin.test.*

class SchemaTests {
    @Test
    fun `create schema`() {
        val one = Schema(name="one", elements=listOf(Schema.Element("a"), Schema.Element("b")))
        assertNotNull(one)
    }

    @Test
    fun `compare schemas`() {
        val one = Schema(name="one", elements=listOf(Schema.Element("a"), Schema.Element("b")))
        val oneAgain = Schema(name="one", elements=listOf(Schema.Element("a"), Schema.Element("b")))
        val two = Schema(name="two", elements=listOf(Schema.Element("a"), Schema.Element("b")))
        assertEquals(one, oneAgain)
        assertNotEquals(one, two)
    }

    @Test
    fun `find element`() {
        val one = Schema(name="one", elements=listOf(Schema.Element("a"), Schema.Element("b")))
        assertEquals(one.findElement("a"), Schema.Element("a"))
        assertNull(one.findElement("c"))
    }

    @Test
    fun `diff elements`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a")))
        val two = Schema(name = "two", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        assertEquals(listOf(Schema.Element("b")), two.diffElements(one))
    }

    @Test
    fun `union elements` () {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a")))
        val two = Schema(name = "two", elements = listOf(Schema.Element("a"), Schema.Element("b")))

        assertEquals(listOf(Schema.Element("a"), Schema.Element("b")), two.unionElements(one))
        assertEquals(listOf(Schema.Element("a"), Schema.Element("b")), one.unionElements(two))
    }
}