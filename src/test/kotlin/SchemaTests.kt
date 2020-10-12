package gov.cdc.prime.router

import kotlin.test.*

class SchemaTests {
    @Test
    fun `create schema`() {
        val one = Schema(name="one", elements=listOf(Element("a"), Element("b")))
        assertNotNull(one)
    }

    @Test
    fun `compare schemas`() {
        val one = Schema(name="one", elements=listOf(Element("a"), Element("b")))
        val oneAgain = Schema(name="one", elements=listOf(Element("a"), Element("b")))
        val two = Schema(name="two", elements=listOf(Element("a"), Element("b")))
        assertEquals(one, oneAgain)
        assertNotEquals(one, two)
    }

    @Test
    fun `find element`() {
        val one = Schema(name="one", elements=listOf(Element("a"), Element("b")))
        assertEquals(one.findElement("a"), Element("a"))
        assertNull(one.findElement("c"))
    }

    @Test
    fun `diff elements`() {
        val one = Schema(name = "one", elements = listOf(Element("a")))
        val two = Schema(name = "two", elements = listOf(Element("a"), Element("b")))
        assertEquals(listOf(Element("b")), two.diffElements(one))
    }

    @Test
    fun `union elements` () {
        val one = Schema(name = "one", elements = listOf(Element("a")))
        val two = Schema(name = "two", elements = listOf(Element("a"), Element("b")))

        assertEquals(listOf(Element("a"), Element("b")), two.unionElements(one))
        assertEquals(listOf(Element("a"), Element("b")), one.unionElements(two))
    }
}