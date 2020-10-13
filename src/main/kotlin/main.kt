package gov.cdc.prime.router

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path


class RouterCli : CliktCommand(help = "Send health messages to their destinations") {
    private val schemaName by option("--schema", help = "Schema name to use")
        .prompt()
    private val output by option("--out", "--output", help = "output filename")
    private val outputDirectory by option("--out_dir", "--output_dir", help = "output directory")
        .file(mustExist = true, mustBeWritable = true)
        .default(File("."))
    private val input by argument(name = "test_file", help = "file to route")
        .file(mustExist = true, mustBeReadable = true)

    private fun writeFile(prefix: String = "", block: (stream: OutputStream) -> Unit) {
        val fileName = (prefix + if (prefix.isEmpty()) "" else "-") + (output ?: input.name)
        val outputFile = File(Path.of(outputDirectory.absolutePath, "/${fileName}").toString())
        echo("Write to: ${outputFile.absolutePath}")
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }
        outputFile.outputStream().use {
            block(it)
        }
    }

    private fun postHttp(address: String, block: (stream: OutputStream) -> Unit) {
        echo("Sending to: ${address}")
        val urlObj = URL(address)
        val connection = urlObj.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        val outputStream = connection.getOutputStream()
        outputStream.use {
            block(it)
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            echo("connection: ${connection.responseCode}")
        }
    }


    override fun run() {
        // Load the schema
        DirectoryManager.loadSchemaCatalog()
        DirectoryManager.loadReceiversList()
        echo("Loaded schema and receivers")

        // Open the file
        echo("Opened: ${input.absolutePath}")

        // Parse as CSV
        val inputRows: CsvRows = Message.readCsv(input.inputStream())

        // Parse the CSV
        val schema = DirectoryManager.schemas[schemaName] ?: error("Invalid schema name")
        val messages = Message.decodeCsv(schema, inputRows)
        echo("processed the file")

        if (output != null) {
            // Process one file
            writeFile() {
                val rows = Message.encodeCsv(messages)
                Message.writeCsv(it, rows)
            }
        } else {
            // Use receivers
            val routedMsgs: Map<String, List<Message>> = Message.splitMessages(messages, DirectoryManager.receivers)
            routedMsgs.forEach { (rec, msgs) ->
                if (msgs.isEmpty()) return@forEach
                val receiver = DirectoryManager
                writeFile(rec) {
                    val rows = Message.encodeCsv(msgs)
                    Message.writeCsv(it, rows)
                }
            }
        }
    }
}

fun main(args: Array<String>) = RouterCli().main(args)

