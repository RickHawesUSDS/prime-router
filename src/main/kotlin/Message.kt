package gov.cdc.prime.router

typealias CsvRows = List<List<String>>

data class Message(
    val schema: Schema,
    val values: Map<String, String>
) {
    fun isA(schema: Schema): Boolean {
        return schema == this.schema
    }

    fun validateValues(): Boolean {
        error("Not implemented")
    }

    fun patternMatch(patterns: Map<String, String>): Boolean {
        patterns.forEach { (key, value) ->
            if (values[key]?.matches(Regex(value)) != true) {
                return false
            }
        }
        return true
    }

    companion object {
        fun isValidCsv(schema: Schema, rows: CsvRows): Boolean {
            return buildColumnMap(schema, rows) != null
        }

        fun buildColumnMap(schema: Schema, rows: CsvRows): Map<String, Int>? {
            if (rows.isEmpty()) return null

            return schema.allElements.map { (name) ->
                for (col in rows[0].indices) {
                    if (name == rows[0][col]) {
                        return@map name to col
                    }
                }
                return null
            }.toMap()
        }

        fun decodeCsv(schema: Schema, rows: CsvRows): List<Message> {
            if (rows.isEmpty() || rows.size == 1) return emptyList()

            val columnMap = buildColumnMap(schema, rows) ?: error("Invalid header")
            val elements = schema.allElements

            return (1 until rows.size).map { rowIndex: Int ->
                val cols = rows[rowIndex]
                val values = cols.indices.map { colIndex: Int ->
                    val elementName = elements[colIndex].name
                    val value = cols[columnMap.getValue(elementName)]
                    elementName to value
                }.toMap()
                Message(schema, values)
            }
        }

        fun encodeCsv(messages: List<Message>): CsvRows {
            if (messages.isEmpty()) return emptyList()
            val elements: List<Element> = messages[0].schema.allElements
            val header: List<String> = elements.map { it.name }
            val rows: CsvRows = messages.map { message: Message ->
                elements.map { (name) ->
                    message.values.getValue(name)
                }
            }
            return listOf(header) + rows
        }

        fun splitMessages(messages: List<Message>, receivers: Map<String, Receiver>) : Map<String, List<Message>> {
            var output = receivers.map { it.key to ArrayList<Message>() }.toMap()
            messages.forEach { message ->
                receivers.forEach { (key, receiver) ->
                    if (message.patternMatch(receiver.topics[0].patterns)) {
                        output[key]?.add(message)
                    }
                }
            }
            return output
        }
    }
}