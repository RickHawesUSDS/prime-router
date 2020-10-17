@file:Suppress("Destructure")

package gov.cdc.prime.router

import tech.tablesaw.api.ColumnType
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.columns.Column
import tech.tablesaw.io.csv.CsvReadOptions
import java.io.InputStream
import java.io.OutputStream

class MappableTable {
    val name: String
    val schema: Schema
    val isRaw: Boolean
    val rowCount: Int get() = this.table.rowCount()

    private val table: Table

    enum class StreamType { CSV }

    constructor(name: String, schema: Schema, values: List<List<String>> = emptyList()) {
        this.name = name
        this.schema = schema
        this.isRaw = true
        this.table = Table.create(name, valuesToRawColumns(schema, values))
    }

    private fun valuesToRawColumns(schema: Schema, values: List<List<String>>): List<Column<*>> {
        return schema.elements.mapIndexed { index, element ->
            StringColumn.create(
                element.name,
                values.map { it[index] }
            )
        }
    }

    constructor(name: String, schema: Schema, csvInputStream: InputStream, streamType: StreamType) {
        this.name = name
        this.schema = schema
        this.isRaw = true

        when (streamType) {
            StreamType.CSV ->
                this.table = Table.read().usingOptions(
                    CsvReadOptions.builder(csvInputStream)
                        .columnTypes(Array(schema.elements.size) { ColumnType.STRING })
                        .build()
                )
        }

    }

    fun isEmpty(): Boolean {
        return table.rowCount() == 0
    }

    fun write(outputStream: OutputStream, streamType: StreamType = StreamType.CSV) {
        if (isEmpty()) return

        when (streamType) {
            StreamType.CSV -> table.write().csv(outputStream)
        }
    }

    /* TODO
fun patternMatch(patterns: Map<String, String>): Boolean {

patterns.forEach { (key, pattern) ->
    val regex = Regex(pattern)
    val value = values.getOrDefault(key, "")
    if (!value.matches(regex)) {
        return false
    }
}
        return true
    }
*/

    companion object {


        /* TODO
        fun splitMessages(messages: List<MappableTable>, receivers: Map<String, Receiver>): Map<String, List<MappableTable>> {
            val output = receivers.map { it.key to ArrayList<MappableTable>() }.toMap()
            messages.forEach { message ->
                receivers.forEach { (key, receiver) ->
                    receiver.topics.forEach(fun(topic: Receiver.Topic) {
                        if (topic.schema != message.schema.name) return
                        if (message.patternMatch(topic.patterns)) {
                            output[key]?.add(message)
                        }
                    })
                }
            }
            return output
        }
         */
    }
}