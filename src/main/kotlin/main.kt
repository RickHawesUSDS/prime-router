package gov.cdc.prime.router

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import java.io.File
import java.nio.file.Path

class RouterCli: CliktCommand(help="Send health messages to their destinations") {
    private val schemaName by option("--schema", help="Schema name to use")
        .prompt()
    private val output by option("--out", "--output", help="output filename")
    private val outputDirectory by option("--out_dir", "--output_dir", help="output directory")
        .file(mustExist = true, mustBeWritable = true)
        .default(File("."))
    private val input by argument(name="test_file", help="file to route")
        .file(mustExist = true, mustBeReadable = true)


    override fun run() {
        // Load the schema
        DirectoryManager.loadSchemaCatalog()
        DirectoryManager.loadReceiversList()
        echo("Loaded schema and receivers")

        // Open the file
        echo("Opened: ${input.absolutePath}")

        // Parse as CSV
        val inputRows: List<List<String>> = CsvReader().readAll(input)

        // Parse the CSV
        val schema = DirectoryManager.schemas[schemaName] ?: error("Invalid schema name")
        val messages = Message.decodeCsv(schema, inputRows)
        echo("processed the file")

        // Write the file
        val outputRows = Message.encodeCsv(messages)

        // Serialize as CSV
        val fileName = output ?: input.name
        val outputFile = File(Path.of(outputDirectory.absolutePath,"/${fileName}").toString())
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        CsvWriter().open(outputFile, append = false) {
            writeAll(outputRows)
        }
        echo("write: ${outputFile.toString()}")
    }
}

fun main(args: Array<String>) = RouterCli().main(args)

