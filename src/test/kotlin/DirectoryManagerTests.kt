package gov.cdc.prime.router

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.StringReader
import kotlin.test.*

class DirectoryManagerTests {
    val recieversYaml = """
            ---
            receivers:
              # Arizona PHD
              - name: phd1
                description: Arizona PHD
                topics:
                  - schema: covid-19
                    patterns: {observation: "covid-19:*", state: AZ}
                    transforms: {deidentify: false}
                    address: phd1
                    format: CSV
        """.trimIndent()

    @Test
    fun `test loading schema catalog`() {
        val catalog = javaClass.getResourceAsStream("/unit_test_files/test_schema_catalog.yml")
        DirectoryManager.loadSchemaCatalog(catalog)
        assertNotNull(DirectoryManager.schemas["lab_test_results_schema"])
    }

    @Test
    fun `test loading two schemas`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a")))
        val two = Schema(name = "two", elements = listOf(Schema.Element("a"), Schema.Element("b")))
        DirectoryManager.loadSchemas(listOf(one, two))
        assertEquals(2, DirectoryManager.schemas.size)
        assertNotNull(DirectoryManager.schemas["one"])
    }

    @Test
    fun `test loading extended schema`() {
        val one = Schema(name = "one", elements = listOf(Schema.Element("a")))
        val two = Schema(name = "two", elements = listOf(Schema.Element("b"), Schema.Element("c")), extends = "one")

        DirectoryManager.loadSchemas(listOf(one, two))
        assertEquals(2, DirectoryManager.schemas.size)
        assertNotNull(DirectoryManager.schemas["one"])
        assertEquals(3, DirectoryManager.schemas["two"]!!.allElements.size)

        DirectoryManager.loadSchemas(listOf(two, one))
        assertEquals(3, DirectoryManager.schemas["two"]!!.allElements.size)
        assertEquals("a", DirectoryManager.schemas["two"]!!.allElements[0].name)
    }

    @Test
    fun `test loading a receiver`() {
        val input = ByteArrayInputStream(recieversYaml.toByteArray())
        DirectoryManager.loadReceiversList(input)
        assertEquals(1, DirectoryManager.receivers.size)
        assertEquals(2, DirectoryManager.receivers["phd1"]!!.topics[0].patterns.size)
    }
}