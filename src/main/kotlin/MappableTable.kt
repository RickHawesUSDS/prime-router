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

    fun getString(row: Int, colName: String, ): String? {
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

    fun filterByReceiver(receivers: List<Receiver>): List<MappableTable> {
        return receivers.mapNotNull { receiver: Receiver ->
            if (receiver.schema != schema.name) return@mapNotNull null
            filter("${receiver.name}-${name}", receiver.patterns)
        }
    }
}