package gov.cdc.prime.router

import kotlin.test.*

class SchemaManagerTests {
    @Test
    fun `test loading schema catalog`() {
        val catalog = javaClass.getResourceAsStream("/unit_test_files/test_schema_catalog.yml")
        SchemaManager.loadSchemaCatalog(catalog)
        assertNotNull(SchemaManager.schemas["lab_test_results_schema"])
    }

    @Test
    fun `test loading two schemas`() {
        val one = Schema(name = "one", elements = listOf(Element("a")))
        val two = Schema(name = "two", elements = listOf(Element("a"), Element("b")))
        SchemaManager.loadSchemas(listOf(one, two))
        assertEquals(2, SchemaManager.schemas.size)
        assertNotNull(SchemaManager.schemas["one"])
    }

    @Test
    fun `test loading extended schema`() {
        val one = Schema(name = "one", elements = listOf(Element("a")))
        val two = Schema(name = "two", elements = listOf(Element("b"), Element("c")), extends = "one")

        SchemaManager.loadSchemas(listOf(one, two))
        assertEquals(2, SchemaManager.schemas.size)
        assertNotNull(SchemaManager.schemas["one"])
        assertNotNull(SchemaManager.flattenSchemas["two"])
        assertEquals(3, SchemaManager.flattenSchemas["two"]!!.elements.size)

        SchemaManager.loadSchemas(listOf(two, one))
        assertEquals(3, SchemaManager.flattenSchemas["two"]!!.elements.size)
        assertEquals("a", SchemaManager.flattenSchemas["two"]!!.elements[0].name)
    }

}