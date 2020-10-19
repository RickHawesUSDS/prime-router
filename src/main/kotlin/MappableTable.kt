@file:Suppress("Destructure")

package gov.cdc.prime.router

import tech.tablesaw.api.ColumnType
import tech.tablesaw.api.StringColumn
import tech.tablesaw.api.Table
import tech.tablesaw.columns.Column
import tech.tablesaw.io.csv.CsvReadOptions
import tech.tablesaw.selection.Selection
import java.io.InputStream
import java.io.OutputStream

class MappableTable {
    val name: String
    val schema: Schema
    val rowCount: Int get() = this.table.rowCount()

    // The use of a TableSaw is an implementation detail hidden by this class
    // The TableSaw table is mutable, while this class is has immutable semantics
    //
    private val table: Table

    enum class StreamType { CSV }

    constructor(name: String, schema: Schema, values: List<List<String>> = emptyList()) {
        this.name = name
        this.schema = schema
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

        when (streamType) {
            StreamType.CSV ->
                this.table = Table.read().usingOptions(
                    CsvReadOptions.builder(csvInputStream)
                        .separator(',')
                        .lineEnding("\n")
                        .header(true)
                        .columnTypes(Array(schema.elements.size) { ColumnType.STRING })
                        .build()
                )
        }
    }

    constructor(name: String, schema: Schema, table: Table) {
        this.schema = schema
        this.name = name
        this.table = table
    }

    fun copy(name: String = this.name): MappableTable {
        return MappableTable(name, this.schema, this.table.copy())
    }

    fun isEmpty(): Boolean {
        return table.rowCount() == 0
    }

    fun getString(row: Int, colName: String): String? {
        return table.getString(row, colName)
    }

    fun write(outputStream: OutputStream, streamType: StreamType = StreamType.CSV) {
        if (isEmpty()) return

        when (streamType) {
            StreamType.CSV -> table.write().csv(outputStream)
        }
    }

    fun concat(name: String, appendTable: MappableTable): MappableTable {
        if (appendTable.schema != this.schema) error("concat a table with a different schema")
        val newTable = this.table.copy().append(appendTable.table)
        return MappableTable(name, this.schema, newTable)
    }

    fun filter(name: String, patterns: Map<String, String>): MappableTable {
        val combinedSelection = Selection.withRange(0, table.rowCount())
        patterns.forEach { (col, pattern) ->
            val columnSelection = table.stringColumn(col).matchesRegex(pattern)
            combinedSelection.and(columnSelection)
        }
        val filteredTable = table.where(combinedSelection)
        return MappableTable(name, this.schema, filteredTable)
    }

    fun routeByReceiver(receivers: List<Receiver>): List<MappableTable> {
        val onTopicReceivers = receivers.filter { it.topic == schema.topic }
        return onTopicReceivers.map { receiver: Receiver ->
            val outputName = "${receiver.name}-${name}"
            val input: MappableTable = if (receiver.schema != schema.name) {
                val toSchema =
                    Schema.schemas[receiver.schema] ?: error("${receiver.schema} schema is missing from catalog")
                val mapping = schema.buildMapping(toSchema)
                this.applyMapping(outputName, mapping)
            } else {
                this
            }
            input.filter(name = outputName, patterns = receiver.patterns)
        }
    }

    fun applyMapping(name: String, mapping: Schema.Mapping): MappableTable {
        val columns = mapping.toSchema.elements.map { buildColumn(mapping, it) }
        val newTable = Table.create(columns)
        return MappableTable(name, mapping.toSchema, newTable)
    }

    private fun buildColumn(mapping: Schema.Mapping, toElement: Schema.Element): StringColumn {
        return when (toElement.name) {
            in mapping.useFromName -> {
                table.stringColumn(mapping.useFromName[toElement.name]).copy().setName(toElement.name)
            }
            in mapping.useDefault -> {
                val defaultValues = Array(table.rowCount()) { (toElement.default ?: "") }
                StringColumn.create(toElement.name, defaultValues.asList())
            }
            else -> error("missing mapping for element: ${toElement.name}")
        }
    }
}