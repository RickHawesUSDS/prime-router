package gov.cdc.prime.router

typealias CsvRows = List<List<String>>

fun Message.Companion.isValidCsv(schema: Schema, rows: CsvRows) : Boolean {
    return buildColumnMap(schema, rows) != null
}

fun Message.Companion.buildColumnMap(schema: Schema, rows: CsvRows) : Map<String,Int>? {
    if (rows.isEmpty()) return null

    return schema.allElements.map { element: Element ->
        for(col in rows[0].indices) {
            if (element.name == rows[0][col]) {
                return@map element.name to col
            }
        }
        return null
    }.toMap()
}

fun Message.Companion.decodeCsv(schema: Schema, rows: CsvRows) : List<Message> {
    if (rows.isEmpty() || rows.size == 1) return emptyList()

    val columnMap = buildColumnMap(schema, rows) ?: error("Invalid header")
    val elements = schema.allElements

    return (1 until rows.size).map { rowIndex:Int ->
        val cols = rows[rowIndex]
        val values = cols.indices.map {colIndex:Int ->
            val elementName = elements[colIndex].name
            val value = cols[columnMap.getValue(elementName)]
            elementName to value
        }.toMap()
        Message(schema, values)
    }
}

fun Message.Companion.encodeCsv(messages: List<Message>) : CsvRows {
    if (messages.isEmpty()) return emptyList()
    val elements: List<Element> = messages[0].schema.allElements
    val header: List<String> = elements.map {it.name}
    val rows: CsvRows = messages.map { message: Message ->
        elements.map { element: Element ->
            message.values.getValue(element.name)
        }
    }
    return listOf(header) + rows
}
