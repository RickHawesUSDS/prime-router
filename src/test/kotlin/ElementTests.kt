package gov.cdc.prime.router

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ElementTests {

    @Test
    fun `create element`() {
        val elem1 = Schema.Element(name = "first", type = Schema.Element.Type.NUMERIC)
        assertNotNull(elem1)
    }

    @Test
    fun `compare elements`() {
        val elem1 = Schema.Element(name = "first", type = Schema.Element.Type.NUMERIC)
        val elem2 = Schema.Element(name = "first", type = Schema.Element.Type.NUMERIC)
        assertEquals(elem1, elem2)

        val elem3 = Schema.Element(name = "first", type = Schema.Element.Type.TEXT)
        assertNotEquals(elem1, elem3)
    }

    @Test
    fun `test schema element`() {
        val elem1 = Schema.Element(name = "first", type = Schema.Element.Type.NUMERIC)
        val elem2 = Schema.Element(name = "first", type = Schema.Element.Type.NUMERIC)
        assertEquals(elem1, elem2)

        val elem3 = Schema.Element(name = "first", type = Schema.Element.Type.TEXT)
        assertNotEquals(elem1, elem3)
    }
}