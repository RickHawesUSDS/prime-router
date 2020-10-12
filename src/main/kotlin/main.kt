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
import java.nio.file.Paths

class RouterCli: CliktCommand(help="Send health messages to their destinations") {
    private val schemaName by option("--schema", help="Schema name to use")
        .prompt()
    private val outputDirectory by option("--output_dir", help="output directory")
        .file(mustExist = true, mustBeWritable = true)
        .default(File("routed_files"))
    private val input by argument(name="schema", help="file to route")
        .file(mustExist = true, mustBeReadable = true)


    override fun run() {
        // Load the schema
        SchemaManager.loadSchemaCatalog()
        echo("Loaded schema")

        // Open the file
        echo("Opened: ${input.absolutePath}")

        // Parse as CSV
        val inputRows: List<List<String>> = CsvReader().readAll(input)

        // Parse the CSV
        val schema = SchemaManager.schemas[schemaName] ?: error("Invalid schema name")
        //val messages = MessageSerializers.decodeCsv(schema, inputRows)
        echo("processed the file")

        // Write the file
        //val outputRows = MessageSerializers.encodeCsv(messages)

        // Serialize as CSV
        val outputPath = Path.of(outputDirectory.absolutePath,"/${input.name}")
        val output = File(outputPath.toString())
        if (!output.exists()) output.createNewFile()
        CsvWriter().open(output,append = false) { writeAll(inputRows) }
        echo("write: ${output.toString()}")
    }
}

fun main(args: Array<String>) = RouterCli().main(args)

