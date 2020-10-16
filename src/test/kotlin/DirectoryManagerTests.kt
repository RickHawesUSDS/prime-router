package gov.cdc.prime.router

import java.io.ByteArrayInputStream
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
    fun `test loading a receiver`() {
        val input = ByteArrayInputStream(recieversYaml.toByteArray())
        DirectoryManager.loadReceiversList(input)
        assertEquals(1, DirectoryManager.receivers.size)
        assertEquals(2, DirectoryManager.receivers["phd1"]!!.topics[0].patterns.size)
    }
}