package gov.cdc.prime.router

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class RouterCli : CliktCommand(name = "prime", help = "Send health messages to their destinations") {
    private val inputFiles by option("--input", help = "<file1>, <file2>, ...").split(",")
    private val inputSchemas by option("--input_schema", help = "<schema_name>, <schema_name>, ...").split(",")
    //val inputDirs by option("--input_dir", help = "<dir1>, <dir1>").split(",")

    private val validate by option("--validate", help = "Validate stream").flag(default = true)
    private val route by option("--route", help = "route to receivers lists").flag(default = false)
    private val partitionBy by option("--partition_by", help = "<col> to partition")
    private val send by option("--send", help = "send to a receiver if specified")

    private val outputFile by option("--output", help = "<file> not compatible with route or partition")
    private val outputDir by option("--output_dir", help = "<directory>")
    private val outputSchema by option("--output_schema", help = "<schema_name> or use input schema if not specified")


    data class DataSet(val name: String, val schema: Schema, val messages: List<Message>)

    private fun readDataSetsFromFile(readBlock: (schema: Schema, stream: InputStream) -> List<Message>): List<DataSet> {
        val inputDataSets = ArrayList<DataSet>()
        if (inputSchemas == null) error("Schema is not specified. Use the --inputSchema option")
        for (i in 0 until (inputFiles?.size ?: 0)) {
            val fileName = inputFiles!![i]
            val file = File(fileName)
            if (!file.exists()) error("$fileName does not exist")
            echo("Opened: ${file.absolutePath}")

            val schemaName =
                if (i < inputSchemas!!.size)
                    inputSchemas!![i]
                else
                    inputSchemas!!.last()
            val schema = DirectoryManager.schemas[schemaName] ?: error("Cannot find the $schemaName schema")

            val messages = readBlock(schema, file.inputStream())

            inputDataSets.add(DataSet(file.name, schema, messages))
        }
        return inputDataSets
    }

    private fun writeDataSetsToFile(
        dataSets: List<DataSet>,
        writeBlock: (schema: Schema, messages: List<Message>, stream: OutputStream) -> Unit
    ) {
        if (outputDir == null && outputFile == null) return
        dataSets.forEach { (name, schema, messages) ->
            val outputFile = File((outputDir ?: ".") + "/${name}")
            echo("Write to: ${outputFile.absolutePath}")
            if (!outputFile.exists()) {
                outputFile.createNewFile()
            }
            outputFile.outputStream().use {
                writeBlock(schema, messages, it)
            }
        }
    }

    private fun postHttp(address: String, block: (stream: OutputStream) -> Unit) {
        echo("Sending to: $address")
        val urlObj = URL(address)
        val connection = urlObj.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        val outputStream = connection.outputStream
        outputStream.use {
            block(it)
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            echo("connection: ${connection.responseCode}")
        }
    }

    private fun partitionByReceivers(input: List<DataSet>): List<DataSet> {
        echo("partition by receiver")
        val outputDataSets = ArrayList<DataSet>()
        input.forEach { (name, schema, inputMessages) ->
            val output: Map<String, List<Message>> = Message.splitMessages(inputMessages, DirectoryManager.receivers)
            output.forEach { (receiver, splitMessages) ->
                outputDataSets += DataSet("$receiver-${name}", schema, splitMessages)
            }
        }
        return outputDataSets
    }

    private fun partitionByColumn(elementName: String, input: List<DataSet>): List<DataSet> {
        return input
    }

    override fun run() {
        // Load the schema and receivers
        Schema.loadSchemaCatalog()
        DirectoryManager.loadReceiversList()
        echo("Loaded schema and receivers")

        // Gather input datasets
        val inputDataSets = readDataSetsFromFile { schema, stream ->
            Message.readCsv(schema, stream)
        }

        // Transform datasets
        val outputDataSets: List<DataSet> = when {
            route -> partitionByReceivers(inputDataSets)
            partitionBy != null -> partitionByColumn("", inputDataSets)
            else -> inputDataSets
        }

        // Output datasets
        writeDataSetsToFile(outputDataSets) { _, messages, stream ->
            Message.writeCsv(stream, messages)
        }
    }
}

fun main(args: Array<String>) = RouterCli().main(args)

