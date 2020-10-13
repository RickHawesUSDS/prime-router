package gov.cdc.prime.router

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ElementTests {

    @Test
    fun `create element`() {
        val elem1 = Element(name = "first", type = Element.Type.NUMERIC)
        assertNotNull(elem1)
    }

    @Test
    fun `compare elements`() {
        val elem1 = Element(name = "first", type = Element.Type.NUMERIC)
        val elem2 = Element(name = "first", type = Element.Type.NUMERIC)
        assertEquals(elem1, elem2)

        val elem3 = Element(name = "first", type = Element.Type.TEXT)
        assertNotEquals(elem1, elem3)
    }

    @kotlin.test.Test
    fun `test`() {
        val elem1 = Element(name = "first", type = Element.Type.NUMERIC)
        val elem2 = Element(name = "first", type = Element.Type.NUMERIC)
        assertEquals(elem1, elem2)

        val elem3 = Element(name = "first", type = Element.Type.TEXT)
        assertNotEquals(elem1, elem3)
    }
}