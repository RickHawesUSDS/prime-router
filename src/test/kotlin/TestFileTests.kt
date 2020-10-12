package gov.cdc.prime.router

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFileTests {
    @Test
    fun `read the test file`() {
        // Open the file
        val testFile = javaClass.getResourceAsStream("/unit_test_files/lab1-test_results-17-42-31.csv")

        // Parse as CSV
        val inputRows: List<Map<String, String>> = CsvReader().readAllWithHeader(testFile)
        assertEquals(7, inputRows.size)
        assertEquals("lab1", inputRows[0]["lab"])
    }
}